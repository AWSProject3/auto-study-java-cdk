import boto3
import logging

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

    azs = {subnet['AvailabilityZone'] for subnet in subnets}

    logger.info(f"Available AZs: {azs}")
    logger.info(f"Public Subnets: {public_subnets}")
    logger.info(f"Private Subnets: {private_subnets}")

    if len(public_subnets) < len(azs) or len(private_subnets) < len(azs):
        raise Exception('Number of public or private subnets does not match the number of availability zones')

    # Ensure that there are enough subnets for each availability zone
    az_to_public_subnet = {az: [] for az in azs}
    az_to_private_subnet = {az: [] for az in azs}

    for subnet in subnets:
        az = subnet['AvailabilityZone']
        if subnet['MapPublicIpOnLaunch']:
            az_to_public_subnet[az].append(subnet['SubnetId'])
        else:
            az_to_private_subnet[az].append(subnet['SubnetId'])

    # Flatten the lists
    public_subnets = [subnet for subnets in az_to_public_subnet.values() for subnet in subnets]
    private_subnets = [subnet for subnets in az_to_private_subnet.values() for subnet in subnets]

    logger.info(f"Final Public Subnets: {public_subnets}")
    logger.info(f"Final Private Subnets: {private_subnets}")

    return {
        'VpcId': vpc_id,
        'PublicSubnetIds': public_subnets,
        'PrivateSubnetIds': private_subnets
    }
