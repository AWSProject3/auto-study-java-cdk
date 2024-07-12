package aws.vpc.rds;

import aws.vpc.VpcInfraManager;
import java.util.Collections;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.services.ec2.IPeer;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroup.Builder;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.constructs.Construct;

public class RdsConfigurator {
    private final Construct scope;
    private final VpcInfraManager vpcInfraManager;

    public RdsConfigurator(Construct scope, VpcInfraManager vpcInfraManager) {
        this.scope = scope;
        this.vpcInfraManager = vpcInfraManager;
    }

    public void configure(String rdsId, String dbName, String userName, String userPassword) {
        IVpc vpc = vpcInfraManager.findExistingVpc(scope);
        createRds(rdsId, dbName, userName, userPassword, vpc);
    }

    private void createRds(String rdsId, String dbName, String userName, String userPassword, IVpc vpc) {
        SubnetSelection selector = createSubnetSelector();
        SubnetGroup subnetGroup = createDbSubnetGroup(rdsId, vpc, selector);
        SecurityGroup rdsSecurityGroup = createRdsSecurityGroup(rdsId, vpc);
        createDbInstance(rdsId, dbName, userName, userPassword, vpc, subnetGroup, selector, rdsSecurityGroup);
    }

    private SubnetSelection createSubnetSelector() {
        return vpcInfraManager.createPrivateSubnetSelector(scope);
    }

    private SubnetGroup createDbSubnetGroup(String rdsId, IVpc vpc, SubnetSelection selector) {
        return SubnetGroup.Builder.create(scope, rdsId + "-SubnetGroup")
                .description("Subnet group for " + rdsId)
                .vpc(vpc)
                .vpcSubnets(selector)
                .build();
    }

    private SecurityGroup createRdsSecurityGroup(String rdsId, IVpc vpc) {
        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(scope, rdsId + "-SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .description("Security group for RDS instance " + rdsId)
                .build();

        rdsSecurityGroup.addIngressRule(
                createEksSecurityGroup(),
                Port.tcp(3306),
                "Allow MySQL access from EKS pods"
        );

        return rdsSecurityGroup;
    }

    private IPeer createEksSecurityGroup() {
        SecurityGroup securityGroup = Builder.create(scope, "EksSecurityGroup")
                .vpc(getVpc())
                .allowAllOutbound(true)
                .description("Security group for EKS cluster")
                .build();

        securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.allTraffic(),
                "Allow all inbound traffic"

        );
        return securityGroup;
    }

    private IVpc getVpc() {
        return Vpc.fromLookup(scope, "ExistingVPC", VpcLookupOptions.builder()
                .tags(Collections.singletonMap("Name", "auto-study"))
                .build());
    }

    private void createDbInstance(String rdsId, String dbName, String userName, String userPassword, IVpc vpc,
                                  SubnetGroup subnetGroup, SubnetSelection selector, SecurityGroup securityGroup) {
        DatabaseInstance.Builder.create(scope, rdsId)
                .instanceIdentifier("rds-instance")
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps
                        .builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.SMALL))
                .credentials(Credentials.fromPassword(userName, SecretValue.unsafePlainText(userPassword)))
                .vpc(vpc)
                .vpcSubnets(selector)
                .subnetGroup(subnetGroup)
                .securityGroups(Collections.singletonList(securityGroup))
                .allocatedStorage(20)
                .databaseName(dbName)
                .deletionProtection(true)
                .backupRetention(Duration.days(7))
                .build();
    }
}
