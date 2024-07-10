package aws.vpc.rds;

import static software.amazon.awscdk.Duration.days;
import static software.amazon.awscdk.SecretValue.unsafePlainText;

import aws.vpc.common.VpcInfraManager;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance.Builder;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.constructs.Construct;

public class RdsConfig {
    private final Construct scope;
    private final VpcInfraManager vpcInfraManager;

    public RdsConfig(Construct scope, VpcInfraManager vpcInfraManager) {
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
        createDbInstance(rdsId, dbName, userName, userPassword, vpc, subnetGroup, selector);
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

    private void createDbInstance(String rdsId, String dbName, String userName, String userPassword, IVpc vpc,
                                  SubnetGroup subnetGroup, SubnetSelection selector) {
        Builder.create(scope, rdsId)
                .instanceIdentifier(rdsId)
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps
                        .builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.SMALL))
                .credentials(Credentials.fromPassword(userName, unsafePlainText(userPassword)))
                .vpc(vpc)
                .vpcSubnets(selector)
                .subnetGroup(subnetGroup)
                .allocatedStorage(20)
                .databaseName(dbName)
                .deletionProtection(true)
                .backupRetention(days(7))
                .build();
    }
}
