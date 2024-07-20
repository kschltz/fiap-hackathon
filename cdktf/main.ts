import {Construct} from "constructs";
import {App, S3Backend, TerraformOutput, TerraformStack} from "cdktf";
import {CompleteECS} from "./lib/ecs-api";
import {EcsTaskDefinition} from "@cdktf/provider-aws/lib/ecs-task-definition";
import {networkConf} from "./lib/networking";
import {Password} from "@cdktf/provider-random/lib/password";
import {RdsCluster} from "@cdktf/provider-aws/lib/rds-cluster";
import {RandomProvider} from "@cdktf/provider-random/lib/provider";
import {EcsCluster} from "@cdktf/provider-aws/lib/ecs-cluster";

const serviceName = "hackathon-hmed";

class HmedDev extends TerraformStack {
    private readonly _environment = "development";

    constructor(scope: Construct, id: string) {
        super(scope, id);

        const network = networkConf(this, "heimdall-network");


        new RandomProvider(this, "random", {alias: serviceName + "-random"});


        const dbPassword = new Password(this, "heimdall-db-password", {
            length: 16,
            overrideSpecial: "!#$%&*()-_=+[]{}<>:?",
            special: true,
        })

        const postgresRDS = new RdsCluster(this, "postgres", {
            clusterIdentifier: serviceName + "-" + this._environment,
            engine: "aurora-postgresql",
            engineMode: "serverless",
            databaseName: "hmed",
            masterUsername: "hmed",
            backupRetentionPeriod: 3,
            finalSnapshotIdentifier: serviceName + "-" + this._environment + "-" + (new Date().getMilliseconds()),
            masterPassword: dbPassword.result,
            scalingConfiguration: {minCapacity: 2}
        })

        const taskSupplier = (scope: Construct, ecsTaskRoleArn: string,
                              envFileArn: string,
                              image: string,
                              logGroupName: String): EcsTaskDefinition => {
            return new EcsTaskDefinition(scope, "ecs-task", {
                family: "hmed-task",
                cpu: "256",
                memory: "512",
                executionRoleArn: ecsTaskRoleArn,
                requiresCompatibilities: ["FARGATE"],
                networkMode: "awsvpc",
                taskRoleArn: ecsTaskRoleArn,
                skipDestroy: true,
                containerDefinitions: JSON.stringify([{
                    name: "hmed",
                    image: image,
                    portMappings: [{
                        containerPort: 8080,
                        hostPort: 8080
                    }],
                    logConfiguration: {
                        logDriver: "awslogs",
                        options: {
                            "awslogs-group": logGroupName,
                            "awslogs-region": "us-west-1",
                            "awslogs-stream-prefix": "hmed"
                        }
                    },
                    environment: [{name: "HTTP_PORT", value: "8080"},
                        {name: "POSTGRES_PORT", value: "5432"},
                        {name: "POSTGRES_DB", value: postgresRDS.databaseName},
                        {name: "POSTGRES_USER", value: postgresRDS.masterUsername},
                        {name: "POSTGRES_PASSWORD", value: postgresRDS.masterPassword},
                        {name: "POSTGRES_HOST", value: postgresRDS.endpoint},
                    ],
                    environmentFiles: [{
                        value: envFileArn,
                        type: "s3"
                    }]
                }]),

            });
        }

        new TerraformOutput(this, "db-output", {
            sensitive: true,
            value: {
                username: postgresRDS.masterUsername,
                password: postgresRDS.masterPassword,
                endpoint: postgresRDS.endpoint,
            },

        })

        const ecsCluster = new EcsCluster(this, "ecs-cluster", {
            name: serviceName + "-cluster" + this._environment,
            tags: {
                Name: serviceName + "-cluster",
                Environment: this._environment
            }
        })

        new CompleteECS(this, "API", {
            ecsCluster,
            taskDefSupplier: taskSupplier,
            environment: this._environment,
            ecrName: "hmed",
            networking: network
        }, serviceName);
    }
}

const app = new App();

const heimdallApi = new HmedDev(app, "development");
new S3Backend(heimdallApi, {
    bucket: "hackathon-hmed",
    region: "us-west-1",
    key: "development/terraform.tfstate",
})
app.synth();
