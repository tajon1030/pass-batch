
# PT 이용권 관리 서비스(초격차패키지 - Part5)
## 1. 요구사항이해  
배치 : 일괄적으로 데이터를 모아서 처리하는것

### 배치에는 어떤 작업이 있을까?
결제 정산 작업이나  
통계데이터 구축  
예약시간에 광고성 메시지 발송  
대량 데이터를 필요로하는 모델 학습 작업 등  

### Spring Batch 를 사용하는 이유
특정환경에 종속되지않는 JVM의 유연성(플랫폼에 관계없이 서버/클라우드/도커 등에 올려서 플랫폼마다 개발할 필요없다)  
유지보수하기 좋다  
DI를 통해 객체간의 결합을 구성할 수 있고 AOP로 반복적호출을 줄일수있다.  
테스트의 용이함  
데이터 신뢰성을 높일수있음(로깅 등을 통해 파악하고 데이터를 신뢰성있게 보존 가능)  

### Spring Batch != 스케줄러  
스프링배치는 잡을 관리하지만 실행시키는것이 아님  

### Spring Batch의 구성
어플리케이션 레이어: 배치처리를 위한 모든 사용자 코드와 설정 포함
배치코어 : 배치를 정의하는 job, step, 실행에사용되는 jobLauncher, jobParameter 등
배치인프라스트럭처 : 파일 DB등에서 데이터를 읽고 쓸 수 있는 item reader, item writer 등


## 이용권 서비스 요구사항 이해하기
사용자는 N개의 이용권을 가질 수 있다.  
이용권은 횟수가 모두 소진되거나 이용기간이 지나면 만료된다.  
이용권 만료 전 사용자에게 알림을 준다.  
업체에서 원하는 시간을 설정하여 일괄로 사용자에게 이용권을 지급할 수 있다.  
예약된 수업은 10분전 출석 안내 알람을준다.  
수업 종료 시간 시점 수업을 예약한 이용권 횟수를 일괄로 차감한다(-1)  
사용자의 수업예약,출석,이용권 횟수 등의 데이터로 유의미한 통계데이터를 만든다.  

### 기능
#### 배치
- 이용권 만료
- 이용권 일괄 지급
- 수업 전 알림
- 수업 후 이용권 차감
- 통계 데이터 구축

#### 뷰
- 사용자 이용권 조회 페이지
- 관리자 이용권 등록 페이지
- 관리자 통계 조회 페이지

#### API
- 사용자 이용권 조회 API
- 관리자 이용권 등록 API
- 관리자 통계 조회 API

### 시스템 구조
배치: DB를 읽고 업데이트
API: 웹에 데이터를 제공하기위해 DB와 통신하여 웹에 전달


## 프로젝트 설계-1.데이터설계
[한개의 체육관을 기준으로..](https://www.erdcloud.com/d/AqXumeaxvJsH8Mnxp)  
[여러개의 체육관을 기준으로..]()  

## 프로젝트설계-2.Batch 구조 설계(1)
### 배치 개념
- job : 기능 하나하나 ex)이용권을 만료하는 job, 수업전 알람을 주는 job  
- step : 배치처리를 정의하고 제어하는 독립된 작업의 단위. ex) 수업전 알람을 주는 job -> 알람대상 사용자 뽑아오는 step, 알람을 전송하는 step    
크게 tasklet기반 스텝과 chunk기반의 스텝으로 나뉜다.  

#### step
- tasklet step : 간단히 정의한 하나의 작업처리.  단일 작업 처리할때 사용  
청크스텝도 태스클릿 안에 구현가능하다.(하나의 tasklet 안에 로직으로 데이터를 읽고 처리하고 쓰고 구성하면 청크와 동일하게 구현 가능)  
- chunk step : 아이템을 기반으로 읽고(itemReader) 처리하고(itemProcessor필수X) 쓰고(itemWriter)하는 작업  
세가지 루프를 수행하는데, 먼저 itemReader에서 chunk단위(한번에 처리할 데이터 갯수) 정의하여 청크단위로 처리할 아이템을 반복으로 얻어옴  
-> 가져온 아이템갯수만큼 itemProcessor가 item을 진행하여 프로세스 작업을 모두 마치면 한번에 itemWriter로 전달  
(해당 과정들은 chunk가 읽을 아이템이 없을때까지 루프 수행된다)  
청크단위의 트랜잭션 처리하여 커밋과 롤백이 청크단위로 이루어짐  


#### job  
: 유일하고 고유한 순서를 가진 여러 스텝들의 목록. 외부의존성의 영향을 받지않고 실행이 가능해야하는 독립적 작업  
- jobRepository :  
배치수행과 관련된 데이터를 가지고있어서 시작하는시간, 종료하는시간, job의 상태,읽기건수,쓰기건수 등 관리.  
일반적으로 RDB사용  
배치내 대부분 컴포넌트들이 이 데이터를 공유한다.  
- jobLauncher :  
job을 실행하는 역할 및 job을 현재스레드에서 수행할지, 스레드풀을 이용할지, job을 실행할때 필요한 파라밈터는 유효한지와 같은 작업들 함께 실행  
런처를통해 job을 실행하면 job은 정의된 step을 실행하게된다.  

##### jobRepository
기본적으로 rdb에 구성되지만 인메모리방식으로도 존재한다.  
- batch_job_instance : job처음실행시 단일 job instance 데이터가 이 테이블에 등록
- batch_job_execution : job의 실제 실행 기록을 나타냄. job이 실행될때마다 새로운 데이터 생성되고 실행하는 동안에도 주기적으로 상태값이나 다른값들이 업데이트  
- batch_job_execution_context : 배치를 여러번 수행할때 유용하게 사용 가능
- batch_job_execution_params : job이 매번실행될때마다 사용된 파라미터 기록
- batch_step_execution : step의 시작, 완료, 상태에 대한 데이터를 저장하고 읽고 처리하고 쓰고 건너뛰고 하는것들도 저장을 함
- batch_step_execution_context : step에서 batch_job_execution_context과 동일한용도  


## 프로젝트설계-2.Batch 구조 설계(2)
### 이용권만료 job
만료대상을 읽어서 만료상태로 업데이트  
스프링 배치의 itemReader는 일일히 코드로 작성하지않아도 파일에서 읽거나 DB에서 읽은 인풋을 처리하는 방법을 제공한다.  
#### itemReader
itemReader인터페이스의 read메소드는 step내에서 처리할 item 한개를 반환한다.  
itemReader에서 데이터를 가져오는 두가지 기법: Cursor vs Paging  
배치는 보통 대용량 데이터를 다루므로 한번에 데이터를 가져오면 문제가 발생할 수 있다.  
따라서 커서 혹은 페이징 방법으로 처리할 수 있는 만큼의 데이터만 가져오는 것  
- 커서 방식: db와 커넥션을 맺은 후 한번에 하나씩 레코드를 스트리밍하여 다음레코드로 움직인다.(JpaCursorItemReader)  
- 페이징방식: page라고 부르는 chunk크기만큼의 레코드를 가져온다.(JpaPagingItemReader)  
커서는 하나의 커넥션으로 가기때문에 페이징보다는 성능이 빠를수있으나 수행시간이 오래걸릴경우 커넥션이 끊길수있다.  
페이징은 성능은 느리나 한페이지를 읽을때마다 커넥션을 맺고 끊기때문에 보다 안정적  
#### itemWriter
itemReaer와 itemProcessor와 다르게 아이템을 건건이 쓰는것이 아닌 청크단위로 쓴다.  
itemWriter의 메소드 write의 인자는 List로 되어있다.  
(JpaItemWriter)  

### 이용권 일괄 지급
한개의 step과 한개의 tasklet으로 구성(AddPassesTasklet)  
tasklet인터페이스의 execute부분에 서비스로직을 구현하는데,  
리턴타입이 RepeatStatus로 finished를 반환할때까지 트랜잭션범위내에서 반복적으로 실행하도록 구현한다.  
SpringBatch는 Step과 하위 Chunk의 반복작업이며 Repeat정책을 따르며  
로직을 반복처리해야하면 RepeatStatus=CONTINUALBE을 반환하고, 처리완료시 FINISHED를 반환하여 작업을 종료한다.  

### 예약 수업 전 알람
단순하게 job내에서 step을 정의된 순서대로 단인 thread로 실행하는것이 아닌 더 많은 사용자들을 처리하기위한(확장하기위한) 병렬처리  
다중thread청크를 사용할 것  
두개의 step으로 구성되어 먼저 알람대상을 가져오고 알람을 전송하는 부분으로 구성할것이며 청크기반스텝에서는 청크단위로 각자 독립적으로 트랜잭션이 적용할것이고 청크처리를 병렬로할것  
스텝은 기본적으로 단일스레드 but 스프링의 task executer를 이용하여 각 스레드가 chunk단위로 실행되도록 멀티스레드 스텝 구현 가능  
chunk의 reader와 writer가 스레드 세이프한지 체크해줘야한다.  
첫번째 스텝에서는 예약 수업에 대한 정보가 들어있는 booking 테이블의 데이터를 가지고 notification데이터를 채워줄 것이고,  
두번째 step에서는 이 데이터를 기반으로 사용자에게 알람을 보내도록 할 것  

### 수업 종료 후 이용권 차감
비동기적 처리를 사용하여 호출에대해 future로 반환하고 AsyncItemWriter에 전달하고 이후에 ItemWriter에 최종 결과값을 넘겨줘서 실행을 위임할것임  
~~사실 이렇게할피룡는 없으나 다양한 구현방식을 익히기 위해 구성~~  
-> booking과 booking에 등록된 pass데이터를 보고 pass내에 있는 remaining count를 업데이트 할 것  
AsyncItemProcessor는 itemProcessor에 별도의 스레드가 할당이 되어서 작업을 처리하는 방식으로,  
한 item이 asyncItemProcessor로 전달될때 새로운 thread를 띄워서 delegate로 기존에 구현을 했던 아이템프로세서에 위임을 하는 동작방식이다.  
아이템프로세서의 결과로 반환된 future는 AsyncItemWriter로 전달된다.  
AsyncItemWriter도 delegate로 아이템라이터를 선언해주면 future를 받아서 결과값들을 itemWriter로 위임한다.  
itemWriter는 future 안에 있는 아이템들을 꺼내서 일괄처리하게되는데,  
이때 프로세서에서 작업중인 비동기 실행의 결과값들을 모두 받아올때까지 대기한다.  
AsyncItemProcessor와 AsyncItemWriter를 같이써야 Future를 처리해줄 수 있다.  
각 스텝내에서 아이템 청크를 병렬로 처리하는 이러한 방식은  
itemProcessor에 병목현상이 있는 경우 멀티스레드로 수행을 하여 성능의 효과를 볼수있는 장점을 가진 방식이다.  


### 통계데이터 생성
시간당으로 쌓아올린 이용권 데이터나 예약, 수업 데이터를 통해 시간당 예약이 몇건이고 횟수가 몇건이 남았고 하는것들을 통계자료로 만드는 하나의 step
데이터를 읽어서 해당 데이터 기반으로 보고서를 만드는 step
step을 병렬로 실행하는 방식을 사용할것(서로 관련없는 작업을 할때 사용해야하는 방식)  
-> 첫번째 step에서 예약정보를 보고 statistics data를 생성  
두번째 step에서 각각 일간,주간 statistics data 출력을 실행  
두번째 step에서 daily와 weekly파일을 쓰는것은 서로 관련이 없는 작업이므로 굳이 순차적으로 진행할 필요가 없음  
이렇게 관련이 없는 동작을 동시에 실행하여 잡의 전반적인 처리량을 늘린다.  

#### BeanScope
스프링의 기본 빈은 싱글톤으로 어플리케이션 실행시 빈이 생성되는데,  
빈의 생성시점을 지정된 scope가 명시된 method가 실행되는 시점으로 지연시킬수있다.  
- @JobScope : Job이 실행될때 생성되고 끝날때 삭제
- @StepScope : Step이 실행될때 생성되고 끝날때 삭제  

spring batch에서 외부 혹은 내부에서 받아서 배치 컴포넌트에서 사용할수있는 파라미터를 jobparmeter라고 하고 이는 스코프빈을 생성할때만 사용이 가능하다.  
이번구현에서 itemReader를 사용할때 jobParameter를 사용할것인데,  
빈스코프인 StepScope를 이용해서 method를 실행하는 시점까지 지연할당할 수 있다.  
또한, 동일한 컴포넌트를 병렬로 처리할때 -ex)step여러개에서 하나의 컴포넌트를 변경하려고 할 경우-  
별도의 빈을 생성하고 관리할 수 있기때문에 유용하게 사용할 수 있다.  