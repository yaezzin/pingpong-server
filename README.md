# Ping Pong

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

#### 1. 회원가입 및 로그인

* 구글 & 네이버 & 애플 소셜로그인 
* ```Spring Security```와 ```JWT```를 이용

#### 2. 팀

* 친구 상태인 유저끼리 팀 초대 가능
* 팀원끼리 할 일을 넘기기

#### 3. 친구

* 

#### 4. 알림 

* 친구 신청, 팀 초대, 할 일 넘기기 등의 이벤트가 발생하면 알림을 발송
* ```Spring SEE```를 통한 실시간 알림

#### 5. 뱃지

* 출석 & 친구 & 할 일 수에 따른 뱃지 획득
* ```Spring Quartz```를 통한 스케쥴링 


#### API 명세서



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
* 👉 [Notion]()
* 👉 [Figma]()


