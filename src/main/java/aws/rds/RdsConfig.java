package aws.rds;

import static software.amazon.awscdk.SecretValue.unsafePlainText;

import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.constructs.Construct;

public class RdsConfig {
    private static final String DEFAULT_VPC_ID = "auto-study-vpc";

    private final Construct scope;

    public RdsConfig(Construct scope) {
        this.scope = scope;
    }

    public void configure(String rdsId, String dbName, String userName, String userPassword) {
        IVpc vpc = lookupExistingVpc();
        createRds(rdsId, dbName, userName, userPassword, vpc);
    }

    private IVpc lookupExistingVpc() {
        return Vpc.fromLookup(scope, DEFAULT_VPC_ID, VpcLookupOptions.builder().build());
    }

    private void createRds(String rdsId, String dbName, String userName, String userPassword, IVpc vpc) {
        SubnetSelection privateSubnets = selectPrivateSubnets();
        SubnetGroup subnetGroup = createDbSubnetGroup(rdsId, vpc, privateSubnets);
        createDbInstance(rdsId, dbName, userName, userPassword, vpc, privateSubnets, subnetGroup);
    }

    private SubnetSelection selectPrivateSubnets() {
        return SubnetSelection.builder()
                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                .build();
    }

    private SubnetGroup createDbSubnetGroup(String rdsId, IVpc vpc, SubnetSelection privateSubnets) {
        return SubnetGroup.Builder.create(scope, rdsId + "-SubnetGroup")
                .description("Subnet group for " + rdsId)
                .vpc(vpc)
                .vpcSubnets(privateSubnets)
                .build();
    }

    private void createDbInstance(String rdsId, String dbName, String userName, String userPassword, IVpc vpc,
                                  SubnetSelection privateSubnets, SubnetGroup subnetGroup) {
        DatabaseInstance.Builder.create(scope, rdsId)
                .instanceIdentifier(rdsId)
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps
                        .builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.SMALL))
                .credentials(Credentials.fromPassword(userName, unsafePlainText(userPassword)))
                .vpc(vpc)
                .vpcSubnets(privateSubnets)
                .subnetGroup(subnetGroup)
                .allocatedStorage(20)
                .databaseName(dbName)
                .deletionProtection(true)
                .backupRetention(software.amazon.awscdk.Duration.days(7))
                .build();
    }
}
