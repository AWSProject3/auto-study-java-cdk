import boto3

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

    return {
        'VpcId': vpc_id,
        'PublicSubnetIds': public_subnets,
        'PrivateSubnetIds': private_subnets
    }
