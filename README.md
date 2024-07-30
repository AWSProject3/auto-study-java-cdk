## 프로젝트 명 : AutoStudy

### 페르소나 Need
사용자의 니즈
- 새로운 언어를 다시 배우려니 거부감이 들지 않고 부담 없이 공부하고 싶음
- 기존에 알던 언어를 기반으로 빠르게 학습하는 방법이 있었으면 좋겠음
- 내 언어의 어떤 기능이 다른 언어에서는 어떻게 사용하는지로 직접적으로 알고 싶음
- 내가 새로운 언어로 작성한 코드가 좋은 코드인지 알고 싶음
- 해당 언어를 어느 정도까지 익혔는지 시각적으로 보고 싶음


### 페르소나 Goal
고객의 목표
- 새로운 언어를 부담없이 쉽게 배우고 싶음
- 최대한 적은 노력으로 새로운 언어를 공부하고 싶음
- 새로운 언어로 작성한 코드가 좋은 코드인지 피드백을 받고 싶음


### How might we?
- 어떻게 이미 알고 있는 언어를 기반으로 한 새로운 언어를 학습할 수 있을까?
- 어떻게 취약 유형을 쉽게 공부할 수 있도록 만들 수 있을까?
- 강의나 책이 아닌 가볍게 접근할 수 있는 방법을 제공해줄 수 있을까?
- 고객이 작성한 코드가 좋은 코드인지 아닌지를 판단할 수 있는 자동화된 방법이 뭐가 있을까?


### 아이디어 통합
- 게미피케이션을 활용해 고객이 부담없이 서비스를 사용할 수 있게 간단한 문제 풀이 서비스 구축
- 코딩 문제에 대한 설명과 고객의 주력언어 코드를 함께 제공하여, 고객이 쉽게
- 학습하고자 하는 언어로 주력 언어를 토대로 새로운 언어 학습 경험 제공
- LLM 으로 문제를 생성하고, 고객이 작성한 코드에 대한 피드백을 제공해 자동화된 서비스 제공


### 서비스 목표
- 내가 알고 있는 프로그래밍 언어로 다른 언어를 빠르게 학습합니다


### 기능
- 생성형 AI로 문제를 생성하고 채점 및 피드백을 제공받습니다
- 질문 사항을 생성형 AI와 채팅을 통해 질의 응답을 받습니다
- 회원은 자신이 푼 문제에 대한 히스토리 목록을 조회하고, 상세보기를 할 수 있습니다.


### user flow
![image](https://github.com/user-attachments/assets/9cd10c12-3b9c-406c-a9bf-5eb2ddd8bfdb)


### 실제 기능 동작 이미지

1. 로그인 이미지
<img width="1280" alt="login1" src="https://github.com/user-attachments/assets/791c9d9d-ea34-4225-8cbd-4e2f5fd9599e">
<img width="1280" alt="login2" src="https://github.com/user-attachments/assets/2c0230d7-0904-4435-a0a2-2ef95bf7ad31">

2. 프로필 이미지
<img width="1280" alt="profile1" src="https://github.com/user-attachments/assets/967e6f25-7b11-4c06-aecd-feb16803c27b">
<img width="1280" alt="profile2" src="https://github.com/user-attachments/assets/0e406e66-fc7d-4bf7-918b-1cf643f608b1">

3. 문제 이미지
<img width="1280" alt="problem1" src="https://github.com/user-attachments/assets/f00c63b8-9aa0-4e77-8bf8-fe87673690bd">

4. 힌트 이미지
<img width="1280" alt="hint1" src="https://github.com/user-attachments/assets/87f97851-5868-42e4-a24a-1e43e1f309c8">

5. 정답 이미지
<img width="1280" alt="answer1" src="https://github.com/user-attachments/assets/0ec321a1-6321-4aad-b176-53f407ed4a1f">
<img width="1280" alt="answer2" src="https://github.com/user-attachments/assets/9db09bbc-aa1c-44e5-b922-be51212b8b3a">

6. 대시보드 이미지
<img width="1280" alt="dashboard1" src="https://github.com/user-attachments/assets/3c768e17-106c-4e12-8243-082174790d70">
<img width="1280" alt="dash2" src="https://github.com/user-attachments/assets/91552d88-c033-47f3-be13-246172eb79c9">
<img width="1280" alt="dash3" src="https://github.com/user-attachments/assets/e3de7f33-3443-41ff-981d-4ffb3feaeba5">

7. Q&A 이미지
<img width="1280" alt="list1" src="https://github.com/user-attachments/assets/601f411f-2350-4de1-a03e-d30cfcd920c7">


### CDK 배포 플로우
![image](https://github.com/user-attachments/assets/61787fe0-c30e-4cfd-b4e1-7fe1501fbc64)


### CDK로 배포된 인프라 콘솔 이미지

1. vpc 이미지
<img width="1280" alt="vpc1" src="https://github.com/user-attachments/assets/e98ec279-655a-4b82-b093-31dc3eb326ea">

2. eks 이미지
<img width="1280" alt="eks1" src="https://github.com/user-attachments/assets/236be135-67fc-47a8-8bc3-e177bc7a7b9d">

3. lambda 이미지
<img width="1280" alt="lambda1" src="https://github.com/user-attachments/assets/ee095609-3914-4e4d-b56c-aa325ee7d5eb">
<img width="1280" alt="lambda2" src="https://github.com/user-attachments/assets/5ac6e4c8-8f72-458d-9161-05001f360bdf">

