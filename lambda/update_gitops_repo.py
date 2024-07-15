import base64
import os
from github import Github


def lambda_handler(event, context):
    repository = get_repo_name_from_ecr(event)

    image_tag = get_image_name_from_ecr(event)

    file_path = 'k8s/base/deployment.yaml'

    repo = get_gitops_repository()

    content = get_deployment_file(file_path, repo)

    updated_content = update_image_tag(content, image_tag, repository)

    repo.update_file(file_path, f"Update image to {image_tag}", updated_content,
                     repo.get_contents(file_path, ref="main").sha, branch="main")

    return {
        'statusCode': 200,
        'body': f"Updated {file_path} with new image tag: {image_tag}"
    }


def get_repo_name_from_ecr(event):
    return event['detail']['repository-name']


def get_image_name_from_ecr(event):
    return event['detail']['image-tag']


def get_gitops_repository():
    g = Github(os.environ['GITHUB_TOKEN'])
    return g.get_repo(os.environ['GITOPS_REPO'])


def get_deployment_file(file_path, repo):
    file = repo.get_contents(file_path, ref="main")
    return base64.b64decode(file.content).decode('utf-8')


def update_image_tag(content, image_tag, repository):
    return content.replace(f'{repository}:*', f'{repository}:{image_tag}')
