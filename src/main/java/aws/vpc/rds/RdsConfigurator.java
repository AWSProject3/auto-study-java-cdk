package aws.vpc.rds;

import aws.vpc.VpcInfraManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.amazon.awscdk.services.rds.PerformanceInsightRetention;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.constructs.Construct;

public class RdsConfigurator {
    private final Construct scope;
    private final VpcInfraManager vpcInfraManager;
    private final RdsSubnetGroup rdsSubnetGroup;
    private final RdsSecurityGroup rdsSecurityGroup;

    public RdsConfigurator(Construct scope, VpcInfraManager vpcInfraManager, RdsSubnetGroup rdsSubnetGroup,
                           RdsSecurityGroup rdsSecurityGroup) {
        this.scope = scope;
        this.vpcInfraManager = vpcInfraManager;
        this.rdsSubnetGroup = rdsSubnetGroup;
        this.rdsSecurityGroup = rdsSecurityGroup;
    }

    public void configure(String rdsId, String dbName, String userName, String userPassword) {
        IVpc vpc = vpcInfraManager.findExistingVpc(scope);
        createRds(rdsId, dbName, userName, userPassword, vpc);
    }

    private void createRds(String rdsId, String dbName, String userName, String userPassword, IVpc vpc) {
        SubnetGroup subnetGroup = rdsSubnetGroup.createDbSubnetGroup(rdsId, vpc);
        SecurityGroup securityGroup = rdsSecurityGroup.createRdsSecurityGroup(rdsId, vpc);
        createDbInstance(rdsId, dbName, userName, userPassword, vpc, subnetGroup, securityGroup);
    }

    private void createDbInstance(String rdsId, String dbName, String userName, String userPassword, IVpc vpc,
                                  SubnetGroup subnetGroup, SecurityGroup securityGroup) {
        DatabaseInstance.Builder.create(scope, rdsId)
                .instanceIdentifier("rds-instance")
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps
                        .builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
                .credentials(Credentials.fromPassword(userName, SecretValue.unsafePlainText(userPassword)))
                .vpc(vpc)
                .subnetGroup(subnetGroup)
                .securityGroups(Collections.singletonList(securityGroup))
                .allocatedStorage(20)
                .databaseName(dbName)
                .deletionProtection(true)
                .backupRetention(Duration.days(7))
                .monitoringInterval(Duration.seconds(60))
                .monitoringRole(createMonitoringRole(rdsId))
                .enablePerformanceInsights(true)
                .performanceInsightRetention(PerformanceInsightRetention.MONTHS_12)
                .build();
    }

    private Role createMonitoringRole(String rdsId) {
        Role monitoringRole = Role.Builder.create(scope, rdsId + "-MonitoringRole")
                .assumedBy(new ServicePrincipal("monitoring.rds.amazonaws.com"))
                .build();

        monitoringRole.addToPolicy(
                PolicyStatement.Builder
                        .create()
                        .effect(Effect.ALLOW)
                        .actions(Arrays.asList(
                                        "logs:CreateLogGroup",
                                        "logs:PutLogEvents",
                                        "logs:DescribeLogStreams",
                                        "logs:CreateLogStream"
                                )
                        )
                        .resources(List.of("arn:aws:logs:*:*:*")).build());

        return monitoringRole;
    }
}
