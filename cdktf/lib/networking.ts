import {Construct} from "constructs";
import {Vpc} from "@cdktf/provider-aws/lib/vpc";
import {Subnet} from "@cdktf/provider-aws/lib/subnet";
import {Fn} from "cdktf";
import {InternetGateway} from "@cdktf/provider-aws/lib/internet-gateway";
import {RouteTable} from "@cdktf/provider-aws/lib/route-table";
import {Route} from "@cdktf/provider-aws/lib/route";
import {RouteTableAssociation} from "@cdktf/provider-aws/lib/route-table-association";

function createSubnets(scope: Construct, name: string, vpc: Vpc, isPublic: boolean, azs: string[]): Subnet[] {
    const subnets: Subnet[] = [];
    for (let i = 0; i < azs.length; i++) {
        let subnetName = name.concat(`-${i}`.concat(isPublic ? "-public" : "-private"));
        const subnet = new Subnet(scope, subnetName, {
            vpcId: vpc.id,
            availabilityZone: azs[i],
            cidrBlock: Fn.cidrsubnet(vpc.cidrBlock, 8, (i + 1) * (isPublic ? 1 : 7)),
            mapPublicIpOnLaunch: isPublic,
            tags: {
                Name: subnetName,
                Environment: "development",
            }
        });
        subnets.push(subnet);
    }
    return subnets;
}

export function networkConf(scope: Construct, id: string, cidrBlock: string = "10.10.0.0/16", azs: string[] = ["us-west-1a", "us-west-1c"]):
    { vpc: Vpc, publicSubnets: Subnet[], privateSubnets: Subnet[], igw: InternetGateway } {
    const vpc = new Vpc(scope, id, {
        cidrBlock: cidrBlock,
        enableDnsHostnames: true,
        enableDnsSupport: true,
        tags: {
            Name: "heimdall-vpc",
            Environment: "development",
        }
    });
    const publicSubnets = createSubnets(scope, "heimdall", vpc, true, azs);
    const privateSubnets = createSubnets(scope, "heimdall", vpc, false, azs);


    const igw = new InternetGateway(scope, "igw", {
        vpcId: vpc.id,
        tags: {
            Name: ("igw"),
        }
    });

    const publicRT = new RouteTable(scope, "public-route-table", {
        vpcId: vpc.id,
    });


    new Route(scope, "public-route", {
        routeTableId: publicRT.id,
        destinationCidrBlock: '0.0.0.0/0',
        gatewayId: igw.id
    })

    publicSubnets.forEach((subnet, idx) => {
        const assocName = "public-rt-assoc-".concat(String(idx))
        new RouteTableAssociation(scope, assocName, {
            routeTableId: publicRT.id,
            subnetId: subnet.id

        })
    });

    return {vpc, publicSubnets, privateSubnets, igw};
}