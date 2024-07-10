package aws.vpc.rds;

import static java.util.stream.Collectors.toList;
import static software.amazon.awscdk.Duration.days;
import static software.amazon.awscdk.SecretValue.unsafePlainText;

import aws.vpc.subnet.dto.BasicInfraDto;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.type.AzType;
import aws.vpc.type.SubnetType;
import java.util.Arrays;
import java.util.List;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance.Builder;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.constructs.Construct;

public class RdsConfig {
    private static final String DEFAULT_VPC_ID = "your-vpc-id";
    private final Construct scope;
    private final BasicInfraDto infraDto;

    public RdsConfig(Construct scope, BasicInfraDto infraDto) {
        this.scope = scope;
        this.infraDto = infraDto;
    }

    public void configure(String rdsId, String dbName, String userName, String userPassword) {
        IVpc vpc = findExistingVpc();
        createRds(rdsId, dbName, userName, userPassword, vpc);
    }

    private IVpc findExistingVpc() {
        return Vpc.fromVpcAttributes(scope, DEFAULT_VPC_ID, VpcAttributes.builder()
                .vpcId(infraDto.vpcId())
                .availabilityZones(getAvailabilityZones())
                .privateSubnetIds(findPrivateSubnetIds())
                .build());
    }

    private List<String> getAvailabilityZones() {
        return Arrays.stream(AzType.values()).map(AzType::getValue).toList();
    }

    private List<String> findPrivateSubnetIds() {
        return infraDto.subnetDtos().stream()
                .filter(subnetDto -> subnetDto.type() == SubnetType.PRIVATE_TYPE)
                .map(SubnetDto::id)
                .toList();
    }

    private void createRds(String rdsId, String dbName, String userName, String userPassword, IVpc vpc) {
        SubnetGroup subnetGroup = createDbSubnetGroup(rdsId, vpc);
        createDbInstance(rdsId, dbName, userName, userPassword, vpc, subnetGroup);
    }

    private SubnetGroup createDbSubnetGroup(String rdsId, IVpc vpc) {
        return SubnetGroup.Builder.create(scope, rdsId + "-SubnetGroup")
                .description("Subnet group for " + rdsId)
                .vpc(vpc)
                .vpcSubnets(createVpcSubnets())
                .build();
    }

    private SubnetSelection createVpcSubnets() {
        return SubnetSelection.builder()
                .subnets(findISubnets())
                .build();
    }

    private List<ISubnet> findISubnets() {
        return findPrivateSubnetIds().stream()
                .map(id -> Subnet.fromSubnetId(scope, "PrivateSubnet" + id, id))
                .collect(toList());
    }

    private void createDbInstance(String rdsId, String dbName, String userName, String userPassword, IVpc vpc,
                                  SubnetGroup subnetGroup) {
        Builder.create(scope, rdsId)
                .instanceIdentifier(rdsId)
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps
                        .builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.SMALL))
                .credentials(Credentials.fromPassword(userName, unsafePlainText(userPassword)))
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(findPrivateSubnetIds().stream()
                                .map(id -> Subnet.fromSubnetId(scope, "PrivateSubnet-" + id, id))
                                .collect(toList()))
                        .build())
                .subnetGroup(subnetGroup)
                .allocatedStorage(20)
                .databaseName(dbName)
                .deletionProtection(true)
                .backupRetention(days(7))
                .build();
    }
}
