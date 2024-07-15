import base64
import os
from github import Github


def lambda_handler(event, context):
    print("Lambda function started")

    repository = get_repo_name_from_ecr(event)
    print(f"Repository name: {repository}")

    image_tag = get_image_name_from_ecr(event)
    print(f"Image tag: {image_tag}")

    file_path = 'k8s/base/deployment.yaml'
    print(f"File path: {file_path}")

    repo = get_gitops_repository()
    print(f"GitOps repository retrieved: {repo.full_name}")

    content = get_deployment_file(file_path, repo)
    print("Deployment file content retrieved")

    updated_content = update_image_tag(content, image_tag, repository)
    print("Image tag updated in file content")

    repo.update_file(file_path, f"Update image to {image_tag}", updated_content,
                     repo.get_contents(file_path, ref="main").sha, branch="main")
    print("File updated in repository")

    print("Lambda function completed successfully")
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
