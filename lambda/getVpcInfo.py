import logging

import boto3

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def handler(event, context):
    vpc_name = event['VpcName']

    ec2 = boto3.client('ec2')

    response = ec2.describe_vpcs(
        Filters=[{'Name': 'tag:Name', 'Values': [vpc_name]}]
    )

    vpcs = response.get('Vpcs', [])
    if not vpcs:
        raise Exception(f'VPC with name {vpc_name} not found')

    vpc_id = vpcs[0]['VpcId']

    response = ec2.describe_subnets(
        Filters=[{'Name': 'vpc-id', 'Values': [vpc_id]}]
    )

    subnets = response.get('Subnets', [])

    public_subnets = [subnet['SubnetId'] for subnet in subnets if subnet['MapPublicIpOnLaunch']]
    private_subnets = [subnet['SubnetId'] for subnet in subnets if not subnet['MapPublicIpOnLaunch']]

    # 가용 영역 정보 수집
    azs = sorted(list(set(subnet['AvailabilityZone'] for subnet in subnets)))

    logger.info(f"VPC ID: {vpc_id}")
    logger.info(f"Available AZs: {azs}")
    logger.info(f"Public Subnets: {public_subnets}")
    logger.info(f"Private Subnets: {private_subnets}")

    # 각 AZ에 대해 public과 private 서브넷이 있는지 확인
    az_to_public_subnet = {az: [] for az in azs}
    az_to_private_subnet = {az: [] for az in azs}

    for subnet in subnets:
        az = subnet['AvailabilityZone']
        if subnet['MapPublicIpOnLaunch']:
            az_to_public_subnet[az].append(subnet['SubnetId'])
        else:
            az_to_private_subnet[az].append(subnet['SubnetId'])

    # 각 AZ에 대해 하나의 public과 private 서브넷만 선택
    public_subnets = [subnets[0] if subnets else None for subnets in az_to_public_subnet.values()]
    private_subnets = [subnets[0] if subnets else None for subnets in az_to_private_subnet.values()]

    # None 값 제거
    public_subnets = [subnet for subnet in public_subnets if subnet is not None]
    private_subnets = [subnet for subnet in private_subnets if subnet is not None]

    logger.info(f"Final Public Subnets: {public_subnets}")
    logger.info(f"Final Private Subnets: {private_subnets}")

    return {
        'VpcId': vpc_id,
        'PublicSubnetIds': ','.join(public_subnets),
        'PrivateSubnetIds': ','.join(private_subnets),
        'AvailabilityZones': ','.join(azs)
    }
