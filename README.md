
> 🏓 **할 일을 넘길 수 있는 일정 관리 애플리케이션**
 
🤖 [Google Play](https://play.google.com/store/apps/details?id=com.pingpong_android)     
🍎 [App Store](https://apps.apple.com/kr/app/%ED%95%91%ED%90%81-%ED%95%A8%EA%BB%98-%ED%95%A0-%EC%9D%BC%EC%9D%84-%EA%B4%80%EB%A6%AC%ED%95%98%EB%8A%94-%EC%95%B1/id6479270351)

![핑퐁 배너](https://github.com/yaezzin/PingPong/assets/97823928/573c893c-04ad-4d39-8e81-3b81bab7e277)


## Description

```
팀플, 여행, 집안일, ... 다양한 모임의 할 일 어떻게 관리하고 계시나요?
서로 해야할 일을 파악하고 어려운 일은 다른 사람에게 넘겨요!
하나씩 주어진 일을 완료할 수록 펑하고 터지는 팝콘, 같이 모아봐요!
```

## Main Feature

### 1. 회원가입 및 로그인

<img width="500" alt="스크린샷 2024-05-20 오후 6 40 21" src="https://github.com/pping-ppong/Server/assets/97823928/5fc8517c-b2c9-43a6-82a7-f5c0b9e42930">

* 구글, 네이버, 애플 소셜 계정을 통해 회원가입 및 로그인을 지원합니다.
* ```Spring Security```와 ```JWT```를 통해 보안과 인증을 구현했습니다.

<br>

### 2. 프로필 및 개인 캘린더

<img width="500" alt="스크린샷 2024-05-20 오후 6 41 52" src="https://github.com/pping-ppong/Server/assets/97823928/8b74747e-b82e-4ef6-b22d-c5c6a5a6d585">

* 사용자는 자신의 닉네임과 프로필 사진을 변경할 수 있습니다.
* 사용자는 자신의 프로필에서 현재 참여 중인 그룹과 획득한 뱃지를 확인할 수 있습니다.
* 친구 수 및 할 일 성취개수 등의 조건을 충족하면 뱃지를 획득할 수 있습니다.
*  ```Spring Quartz```를 통한 특정 시간에 뱃지 획득 작업을 실행합니다.

<br>
<img width="596" alt="스크린샷 2024-05-20 오후 6 47 27" src="https://github.com/pping-ppong/Server/assets/97823928/72d9b14d-e655-44c6-81f5-1ead2ac7c438">

* 사용자는 개별 캘린더에서 자신이 속한 그룹 및 담당 업무를 확인할 수 있습니다.
* 개별 성취율에 따라 색상 및 팝콘의 변화를 확인할 수 있습니다.
* 사용자는 개인 캘린더에서 할 일을 넘기거나 버릴 수 있습니다.

<br>

### 3. 검색 및 최근 검색어

<img width="600" alt="스크린샷 2024-05-20 오후 6 46 12" src="https://github.com/pping-ppong/Server/assets/97823928/94d37b34-3054-4ae8-9ae4-9185edbff8ea">

* 사용자는 닉네임을 통해 친구를 검색할 수 있습니다.
* 검색한 친구 기록은 저장되며, 필요시 최근 검색 기록을 전체 삭제할 수 있습니다.

<br>

### 4. 친구

<img width="500" alt="스크린샷 2024-05-20 오후 6 42 07" src="https://github.com/pping-ppong/Server/assets/97823928/834a01d2-4cfc-49c5-a39c-c727a88bc131">

* 사용자는 다른 사용자에게 친구 요청을 보낼 수 있습니다. 요청을 받은 사용자는 알림창에서 요청을 수락하거나 거절할 수 있습니다.
* 현재 친구 목록을 확인하고, 친구 관계를 관리합니다.

<br>

### 5. 팀 

<img width="500" alt="스크린샷 2024-05-20 오후 6 42 31" src="https://github.com/pping-ppong/Server/assets/97823928/c3ab9d36-6c93-48df-b3b7-1405b3997a74">

* 사용자는 친구로 등록된 다른 사용자들을 자신의 팀으로 초대할 수 있습니다.

<br>  

<img width="500" alt="스크린샷 2024-05-20 오후 6 42 46" src="https://github.com/pping-ppong/Server/assets/97823928/175bd261-9348-489d-b6f2-47c731d7c9aa">
 
* 팀 내에서 할 일을 생성하고, 담당자를 지정할 수 있습니다.
* 팀 내에서 특정 업무를 다른 팀원에게 넘길 수 있는 기능을 제공합니다.
* 팀별 할 일 성취율에 따라 색상 및 팝콘의 변화를 확인할 수 있습니다.

<br>
<img width="600" alt="스크린샷 2024-05-20 오후 6 45 22" src="https://github.com/pping-ppong/Server/assets/97823928/4e8895f2-43e1-4aeb-8733-08da58703cfa">

* 사용자는 자신이 버린 할 일들을 조회할 수 있으며, 방장은 모든 유저의 버린 할 일들을 조회할 수 있습니다.
* 방장의 권한을 가진 사람만이 버린 할 일들을 복구 및 영구 삭제가 가능합니다.

<br>

### 6. 알림 

* 친구 신청, 팀 초대, 할 일 넘기기 등의 다양한 이벤트가 발생하면 사용자에게 알림을 발송합니다
* Spring SEE(Spring Server-Sent Events)를 통해 실시간으로 푸쉬 알림을 수신할 수 있습니다.
  
## Stack
* **Languange** : Java
* **Library & Framewrok** : Spring
* **Database** : AWS RDS (MySQL, MongoDB, Redis)
* **ORM** : JPA
* **Deploy** : AWS EC2

## Project Structure

```
src
└── main
    ├── 🤖 domain
    │   ├── 👥 friend
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── 📷 image
    │   └── 🙍 member
    │   └── ⏰ notification
    │   └── 📣 social
    │   └── 👥 team
    │
    └── 🌎 global
        ├── aop
        ├── common
        ├── config
        └── security
```

## ERD

<img width="600" alt="스크린샷 2024-03-20 오후 6 10 19" src="https://github.com/yaezzin/PingPong/assets/97823928/9f685e9d-b0e5-4e41-b5b6-290cffad5a9e">

## Server Architecture



## CI/CD

* github actions를 활용해서 지속적 통합 및 배포
* ```feature```브랜치에서 ```dev```로 Pull Request를 보내면, CI가 동작
* ```dev```에서 ```release```로 Pull Request를 보내면, CI가 동작되고 Merge가 되면, 운영 리소스에 배포

## Dev Issue

#### ✍ 개발 중 고민했던 내용들 

* [조회 성능에 대한 고민 - QueryDsl](https://github.com/pping-ppong/Server/issues/2)
* [@ManyToOne 연관관계 매핑에 대한 고민](https://github.com/pping-ppong/Server/issues/2)
* [검색 기록 저장 및 조회 기능](https://github.com/pping-ppong/Server/issues/3)
* [AOP를 활용한 중복코드 리팩토링](https://github.com/pping-ppong/Server/issues/4)
* [공통 응답 객체 만들기](https://github.com/pping-ppong/Server/issues/5)
* [No offset 방식 적용하기](https://github.com/pping-ppong/Server/issues/6)
* [알림 서비스에 대한 고민](https://github.com/pping-ppong/Server/issues/6)

## Developer

* ```🤖 Android``` **김민지** ([@mingZZ-3](https://github.com/mingZZ-3))
* ```🍎 iOS```     **성민주** ([@yoogail105](https://github.com/yoogail105))
* ```🛠 Backend``` **전예진** ([@yaezzin](https://github.com/yaezzin))

## ETC

* 👉 [API 명세서](https://docs.google.com/spreadsheets/d/1gironPuvcwKDzbzAOUrbPqZ8V65owz-T/edit#gid=2035882690)
* 👉 [Notion](https://www.notion.so/h2bal/70cfd3b05ab349d9bd54fa47feca75e6?pvs=4)
* 👉 [Figma](https://www.figma.com/design/SkIyYCDBxAN71XowOCR0L9/Design?node-id=0%3A1&t=jUI2TQTMaePkSqVf-1)


