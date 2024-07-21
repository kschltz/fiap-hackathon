import {Construct} from "constructs";
import {AwsProvider} from "@cdktf/provider-aws/lib/provider";
import {TerraformOutput} from "cdktf";
import {IamRole} from "@cdktf/provider-aws/lib/iam-role";
import {IamRolePolicyAttachment} from "@cdktf/provider-aws/lib/iam-role-policy-attachment";
import {EcsCluster} from "@cdktf/provider-aws/lib/ecs-cluster";
import {EcsTaskDefinition} from "@cdktf/provider-aws/lib/ecs-task-definition";
import {EcsService} from "@cdktf/provider-aws/lib/ecs-service";
import {SecurityGroup} from "@cdktf/provider-aws/lib/security-group";
import {Alb} from "@cdktf/provider-aws/lib/alb";
import {AlbTargetGroup} from "@cdktf/provider-aws/lib/alb-target-group";
import {CloudwatchLogGroup} from "@cdktf/provider-aws/lib/cloudwatch-log-group";
import {DataAwsS3Object} from "@cdktf/provider-aws/lib/data-aws-s3-object";
import {IamPolicy} from "@cdktf/provider-aws/lib/iam-policy";
import {AppautoscalingPolicy} from "@cdktf/provider-aws/lib/appautoscaling-policy";
import {AppautoscalingTarget} from "@cdktf/provider-aws/lib/appautoscaling-target";
import {LbListener} from "@cdktf/provider-aws/lib/lb-listener";
import {DataAwsEcrRepository} from "@cdktf/provider-aws/lib/data-aws-ecr-repository";

export class CompleteECS extends Construct {
    public alb: Alb;
    constructor(scope: Construct, id: string,
                cfg: {
                    ecsCluster: EcsCluster,
                    taskDefSupplier: (scope: Construct, ecsTaskRoleArn: string, envFileArn: string, image: string, logGroupName: String) => EcsTaskDefinition,
                    environment: string,
                    ecrName: string,
                    networking: { vpc: any, publicSubnets: any[], privateSubnets: any[] }
                }, serviceName: string, servicePort: number = 8080) {
        super(scope, id);

        new AwsProvider(this, "aws", {
            region: "us-west-1",
            alias: serviceName + "-aws"

        });

        const envFile = new DataAwsS3Object(this, "env-file", {
            bucket: serviceName, key: cfg.environment + "/" + serviceName + "/.env"
        })

        const ecr = new DataAwsEcrRepository(this, "ecr-repo", {
            name: cfg.ecrName,
        });


        const ecsTaskRole = new IamRole(this, "ecs-task-role", {
            name: (serviceName + "-task-role"),
            assumeRolePolicy: JSON.stringify({
                Version: "2012-10-17",
                Statement: [
                    {
                        Effect: "Allow",
                        Principal: {
                            Service: "ecs-tasks.amazonaws.com"
                        },
                        Action: "sts:AssumeRole"
                    },

                ]
            })
        })

        new IamRolePolicyAttachment(this, "ecs-task-policy", {
            policyArn: "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role",
            role: ecsTaskRole.name
        })

        const s3GetObjectPolicy = new IamPolicy(this, "s3-get-object-policy", {
            name: serviceName + "s3-get-object-policy",
            policy: JSON.stringify({
                Version: "2012-10-17",
                Statement: [
                    {
                        Effect: "Allow",
                        Action: "s3:GetObject",
                        Resource: "*"
                    }
                ]
            })
        });

        new IamRolePolicyAttachment(this, "ecs-task-s3-policy", {
            policyArn: s3GetObjectPolicy.arn,
            role: ecsTaskRole.name
        });

        const logGroup = new CloudwatchLogGroup(this, "log-group", {
            name: serviceName + "-log-group",
            retentionInDays: 7

        })

        const taskDef = cfg.taskDefSupplier(this, ecsTaskRole.arn, envFile.arn, ecr.repositoryUrl.concat(":latest"), logGroup.name);

        const albTg = new AlbTargetGroup(this, "target_group", {
            healthCheck: {
                healthyThreshold: 8,
                interval: 300,
                matcher: "200",
                path: "/",
                protocol: "HTTP",
                timeout: 5,
                unhealthyThreshold: 4
            },
            name: serviceName + "-tg",
            port: servicePort,
            protocol: "HTTP",
            tags: {
                Environment: cfg.environment,
                Name: serviceName + "-tg"
            },
            targetType: "ip",
            vpcId: cfg.networking.vpc.id
        });

        const albSG = new SecurityGroup(this, "load_balancer_security_group", {
            egress: [
                {
                    cidrBlocks: ["0.0.0.0/0"],
                    fromPort: 0,
                    ipv6CidrBlocks: ["::/0"],
                    protocol: "-1",
                    toPort: 0,
                },
            ],
            ingress: [
                {
                    cidrBlocks: ["0.0.0.0/0"],
                    fromPort: 0,
                    ipv6CidrBlocks: ["::/0"],
                    protocol: "-1",
                    toPort: 0,
                },
            ],
            tags: {
                Environment: cfg.environment,
                Name: "alb-sg",
            },
            vpcId: cfg.networking.vpc.id,
        });


        const ecsSG = new SecurityGroup(this, "ecs-sg", {
            vpcId: cfg.networking.vpc.id,
            ingress: [{

                fromPort: 0,
                toPort: 0,
                protocol: "-1",
                securityGroups: [albSG.id],

            }],
            egress: [{
                fromPort: 0,
                toPort: 0,
                protocol: "-1",
                cidrBlocks: ["0.0.0.0/0"],
                ipv6CidrBlocks: ["::/0"]
            }]
        });


        this.alb = new Alb(this, "alb", {
            name: serviceName + "-alb",
            internal: false,
            securityGroups: [albSG.id],
            subnets: cfg.networking.publicSubnets.map(subnet => subnet.id),
            tags: {
                Environment: cfg.environment,
                Name: serviceName + "-alb"
            }

        })

        new LbListener(this, "alb-listener", {
            defaultAction: [{
                type: "forward",
                targetGroupArn: albTg.arn
            }],
            loadBalancerArn: this.alb.arn,
            port: servicePort,
            protocol: "HTTP",
        })


        const ecsService = new EcsService(this, "ecs-service", {
            name: serviceName ,
            cluster: cfg.ecsCluster.id,
            desiredCount: 2,
            launchType: "FARGATE",
            taskDefinition: taskDef.arn,
            schedulingStrategy: "REPLICA",
            forceNewDeployment: true,
            loadBalancer: [{
                targetGroupArn: albTg.arn,
                containerName: cfg.ecrName,
                containerPort: servicePort
            }],
            networkConfiguration: {
                subnets: cfg.networking.privateSubnets.map(subnet => subnet.id).concat(cfg.networking.publicSubnets.map(s => s.id)),
                securityGroups: [albSG.id, ecsSG.id],
                assignPublicIp: true
            }
        });


        const scalingTarget = new AppautoscalingTarget(this, "auto-scaling-target", {
            maxCapacity: 10,
            minCapacity: 2,
            resourceId: 'service/' + cfg.ecsCluster.name + '/' + ecsService.name,
            scalableDimension: "ecs:service:DesiredCount",
            serviceNamespace: "ecs"
        })

        new AppautoscalingPolicy(this, "auto-scaling", {
            name: "memory-scaling-policy",
            policyType: "TargetTrackingScaling",
            resourceId: scalingTarget.resourceId,
            scalableDimension: scalingTarget.scalableDimension,
            serviceNamespace: scalingTarget.serviceNamespace,
            targetTrackingScalingPolicyConfiguration: {
                targetValue: 85,
                predefinedMetricSpecification: {
                    predefinedMetricType: "ECSServiceAverageMemoryUtilization"
                }

            }
        });

        new TerraformOutput(this, "env-output", {
            value: envFile.arn
        })

        new TerraformOutput(this, "alb-dns", {
            value: this.alb.dnsName
        })


    }
}