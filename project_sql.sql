SHOW DATABASES ;
SHOW TABLES;
USE book;


-- 멤버 테이블
CREATE TABLE MEMBER (
    IDX BIGINT AUTO_INCREMENT PRIMARY KEY,              -- 기본 키
    ID VARCHAR(100),
    NICK VARCHAR(50) UNIQUE,                   -- 고유 제약 조건
    PWD VARCHAR(512),                           -- 비밀번호
    NAME VARCHAR(100),                         -- 이름
    PHONE VARCHAR(20),                         -- 전화번호
    EMAIL VARCHAR(255),                        -- 이메일
    ZIPCODE VARCHAR(100),                      -- 우편번호
    ADDRESS VARCHAR(200),                      -- 주소
    DETAIL_ADDRESS VARCHAR(200),               -- 상세 주소
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, -- 레코드 생성 시만 현재 시간
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정 시 자동으로 현재 시간 갱신
    AUTHORITY VARCHAR(20) DEFAULT 'ROLE_USER', -- 기본 권한, 기본값 'ROLE_USER'
    ENABLED INT DEFAULT 1,                     -- 활성화 여부, 기본값 1
    SOCIAL_ID VARCHAR(200),                    -- 소셜 아이디
    SOCIAL_PROVIDER VARCHAR(200),              -- 소셜 제공자
    SOCIAL_EMAIL VARCHAR(200),                 -- 소셜 이메일
    FAILED_ATTEMPTS INT,                       -- 실패한 시도 횟수
    ACCOUNT_LOCKED INT,                         -- 계정 잠금 여부
    LAST_LOGIN_DATE DATETIME                   -- 마지막 로그인 날짜
);

INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test1', 'test1',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test1', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test2', 'test2',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test2', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test3', 'test3',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test3', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test4', 'test4',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test4', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test5', 'test5',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test5', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test6', 'test6',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test6', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test7', 'test7',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test7', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test8', 'test8',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test8', 0, 0);
INSERT INTO MEMBER (ID, NICK, PWD, NAME, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
	   	    VALUE('test9', 'test9',  '$2a$10$UdpFAvAK2qMhnk.uVe2MtOKg.LxOhcug0SMH4DhmnpR3YJC4nt5C6', 'test9', 0, 0);

-- 주문 테이블
CREATE TABLE ORDERS (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 주문 고유 ID
    merchant_uid VARCHAR(100) NOT NULL UNIQUE,       -- 고유한 주문 ID (String 타입으로 변경)
    member_id BIGINT NOT NULL,                       -- 회원 테이블의 IDX와 연관된 외래키
    order_date DATETIME NOT NULL,                    -- 주문 날짜
    status VARCHAR(50) NOT NULL,                     -- 주문 상태 (예: ORDERED, CANCELED 등)
    total_amount DOUBLE NOT NULL,                    -- 주문 총액
    shipping_address VARCHAR(200) NOT NULL,          -- 배송 주소
    detail_address VARCHAR(200) NOT NULL,            -- 상세 주소
    recipient_name VARCHAR(100) NOT NULL,            -- 수령인 이름
    recipient_phone VARCHAR(20) NOT NULL,            -- 수령인 연락처
    payment_method VARCHAR(50) NOT NULL,             -- 결제 방법 (예: CARD 등)
    payment_status VARCHAR(50) NOT NULL,             -- 결제 상태 (예: PAID 등)
    book_title VARCHAR(200) NOT NULL,                -- 주문한 책 제목
    book_author VARCHAR(100) NOT NULL,               -- 주문한 책 저자
    book_publisher VARCHAR(100) NOT NULL,            -- 주문한 책 출판사
    book_image_url VARCHAR(500) NOT NULL,            -- 주문한 책 이미지 URL (500자로 확장)
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 레코드 생성 시 현재 시간
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정 시 자동으로 현재 시간 갱신
    CONSTRAINT FK_member_order FOREIGN KEY (member_id) REFERENCES MEMBER(IDX)   -- MEMBER 테이블의 IDX와 연관
);

-- 문의게시판 테이블

CREATE TABLE INQUIRYBOARD (
    QIDX INT NOT NULL AUTO_INCREMENT,            -- 게시글 번호 (PK)
    ORIGINNO INT DEFAULT NULL,                   -- 그룹 번호 (원글 번호)
    GROUPORD INT DEFAULT NULL,                      -- 그룹 내 순서
    GROUPLAYER INT DEFAULT NULL,                    -- 그룹 계층 (답글의 깊이)
    QRESPONSES INT DEFAULT 0,                       -- 답변 여부 (0: 미답변, 1: 답변 완료)
    NICK VARCHAR(50),                               -- 작성자 닉네임
    QTITLE VARCHAR(250),                            -- 게시글 제목
    QCONTENT TEXT,                                  -- 게시글 내용
    QPASS VARCHAR(100),                             -- 글 비밀번호
    QCREATE_DATE DATETIME not null DEFAULT CURRENT_TIMESTAMP,-- 작성일
    QUPDATE_DATE DATETIME,                          -- 수정일
    QVIEW_COUNT INT DEFAULT 0,                      -- 조회수
    QOFILE VARCHAR(255),                            -- 원본 파일명
    QSFILE VARCHAR(255),                            -- 저장된 파일명
    PRIMARY KEY (qidx),                             -- 기본 키 설정
    INDEX idx_origin_no (originNo),                 -- 그룹 번호에 대한 인덱스
    CONSTRAINT fk_parent_qidx FOREIGN KEY (originNo) REFERENCES INQUIRYBOARD (qidx)
        ON DELETE CASCADE                           -- 부모 글 삭제 시 답글도 함께 삭제
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- 판매 게시판
CREATE TABLE SALESBOARD (
    SIDX INT NOT NULL AUTO_INCREMENT,         -- 자동 증가 기본 키
	NICK VARCHAR(50),                         -- 외래 키로 참조하는 NICK
    STITLE VARCHAR(100) NOT NULL, 			  -- 글 제목
    SBOOKTITLE VARCHAR(100) NOT NULL,         -- 책 제목
    SAUTHOR VARCHAR(50) NOT NULL,			  -- 지은이
    SPUBLISHER VARCHAR(50) NOT NULL,		  -- 출판사
    SCONTENT TEXT NOT NULL,                   -- 내용 (CLOB 대신 TEXT 사용)
    SCLASSIFICATION VARCHAR(50) NOT NULL,	  -- 말머리 교환, 나눔, 거래중 ...
    SREGION VARCHAR(50) NOT NULL,			  -- 판매 지역
    SPRICE VARCHAR(50) NOT NULL DEFAULT 0,
    SCREATE_DATE DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 레코드 생성 시만 현재 시간
    SUPDATE_DATE DATETIME, 					  -- 수정 시간 
    SVIEW_COUNT INT DEFAULT 0,                -- 조회 수, 기본값 0
    SLIKE_COUNT INT DEFAULT 0,                -- 좋아요 수, 기본값 0
    LIMAGE VARCHAR(200),
    OIMAGE VARCHAR(200),                      -- 원본 파일
    SIMAGE VARCHAR(200),                      -- 저장된 파일
    PRIMARY KEY (SIDX),                       -- 기본 키 설정
    FOREIGN KEY (NICK) REFERENCES MEMBER(NICK) -- 외래 키 제약 설정 (MEMBER 테이블의 NICK 참조)
    ON DELETE CASCADE                   	  -- 부모가 삭제되면 자식도 삭제
	ON UPDATE CASCADE  						-- 부모가 업데이트되면 자식도 업데이트
);

-- 쪽지 테이블
CREATE TABLE MESSAGE (
    MSIDX INT NOT NULL AUTO_INCREMENT,      -- 쪽지 번호
    SENDER_NICK VARCHAR(50),                -- 보낸 사람 (sender)
    RECEIVER_NICK VARCHAR(50),              -- 받는 사람 (receiver)
    MSTITLE VARCHAR(50) NOT NULL,
    MSCONTENT VARCHAR(300) NOT NULL, 
    MSCREATE_DATE DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 레코드 생성 시만 현재 시간
    READSTATUS TINYINT NOT NULL DEFAULT 0,         -- 읽음 여부 (0 = false, 1 = true)
    VIEWSTATUS TINYINT NOT NULL DEFAULT 1, 
    SENDVIEWSTATUS TINYINT NOT NULL DEFAULT 1, 
    
    PRIMARY KEY (MSIDX),
    FOREIGN KEY (SENDER_NICK) REFERENCES MEMBER(NICK),
    FOREIGN KEY (RECEIVER_NICK) REFERENCES MEMBER(NICK)
);

DROP TABLE MEMBER;
DROP TABLE ORDERS;
DROP TABLE INQUIRYBOARD;
DROP TABLE  if exists INQUIRYBOARD;
DROP TABLE SALESBOARD;
DROP TABLE MESSAGE;