import base64
import os
import yaml
from github import Github


def lambda_handler(event, context):
    repository = get_repo_name_from_ecr(event)
    image_tag = get_image_name_from_ecr(event)

    repo = get_gitops_repository()

    update_values_file(image_tag, repo, repository)

    update_chart_file(repo)

    return {
        'statusCode': 200,
        'body': f"Updated Helm chart with new image: {repository}:{image_tag}"
    }


def get_repo_name_from_ecr(event):
    return event['detail']['repository-name']


def get_image_name_from_ecr(event):
    return event['detail']['image-tag']


def get_gitops_repository():
    g = Github(os.environ['GITHUB_TOKEN'])
    return g.get_repo(os.environ['GITOPS_REPO'])


def update_chart_file(repo):
    chart_path = 'helm/Chart.yaml'
    chart_content = get_yaml_file(chart_path, repo)
    updated_chart = update_chart_yaml(chart_content)
    update_file(repo, chart_path, "Bump chart version", updated_chart)


def update_values_file(image_tag, repo, repository):
    values_path = 'helm/values.yaml'
    values_content = get_yaml_file(values_path, repo)
    updated_values = update_values_yaml(values_content, repository, image_tag)
    update_file(repo, values_path, f"Update image to {repository}:{image_tag}", updated_values)


def get_yaml_file(file_path, repo):
    file = repo.get_contents(file_path, ref="main")
    content = base64.b64decode(file.content).decode('utf-8')
    return yaml.safe_load(content)


def update_chart_yaml(content):
    version_parts = content['version'].split('.')
    version_parts[-1] = str(int(version_parts[-1]) + 1)
    content['version'] = '.'.join(version_parts)
    return yaml.dump(content)


def update_values_yaml(content, repository, image_tag):
    content['image']['repository'] = repository
    content['image']['tag'] = image_tag
    return yaml.dump(content)


def update_file(repo, file_path, commit_message, content):
    repo.update_file(
        file_path,
        commit_message,
        content,
        repo.get_contents(file_path, ref="main").sha,
        branch="main"
    )
