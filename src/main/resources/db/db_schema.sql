CREATE TABLE IF NOT EXISTS creator (
    id      BIGINT          NOT NULL    AUTO_INCREMENT,
    name    VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS course (
    id          BIGINT          NOT NULL    AUTO_INCREMENT,
    creator_id  BIGINT          NOT NULL,

    title       VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk__course__creator_id FOREIGN KEY (creator_id) REFERENCES creator(id)
);



CREATE TABLE IF NOT EXISTS sale_record (
    id          BIGINT          NOT NULL    AUTO_INCREMENT,

    course_id   BIGINT          NOT NULL,
    student_id  BIGINT          NOT NULL,

    amount      BIGINT          NOT NULL,
    paid_at     TIMESTAMP       NOT NULL,

    created_at      TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk__sale_record__course_id FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE IF NOT EXISTS cancel_record (
    id              BIGINT          NOT NULL    AUTO_INCREMENT,

    sale_record_id  BIGINT          NOT NULL,

    amount          BIGINT          NOT NULL,
    cancel_at       TIMESTAMP       NOT NULL,

    created_at      TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk__cancel_record__sale_record_id FOREIGN KEY (sale_record_id) REFERENCES sale_record(id)
);

CREATE TABLE IF NOT EXISTS settlement (
    id                          BIGINT      NOT NULL    AUTO_INCREMENT,
    creator_id                  BIGINT      NOT NULL,
    status                      ENUM('PENDING', 'CONFIRMED', 'PAID') NOT NULL DEFAULT 'PENDING',

    total_sale_amount           BIGINT      NOT NULL    COMMENT '총 판매 금액',
    total_cancel_amount         BIGINT      NOT NULL    COMMENT '총 취소/환불 금액',
    net_sales_amount            BIGINT      NOT NULL    COMMENT '순 판매 금액(총 판매 금액 - 총 취소/환불 금액)',
    settlement_amount           BIGINT      NOT NULL    COMMENT '정산 예정 금액',

    sale_count                  BIGINT      NOT NULL,
    cancel_count                BIGINT      NOT NULL,

    commission_rate             SMALLINT    NOT NULL    COMMENT '반영된 플랫폼 수수료율',
    commission_amount           BIGINT      NOT NULL    COMMENT '반영된 플랫폼 수수료 금액',

    carryover_deduction_amount  BIGINT      NOT NULL    COMMENT '이전 정산월에서 이월되어 이번 정산 금액에서 차감되는 금액',

    settlement_month            DATE        NOT NULL    COMMENT '정산 기준월. 항상 해당 월 1일로 저장',
    settled_at                  TIMESTAMP   NOT NULL,
    confirmed_at                TIMESTAMP   NULL,
    paid_at                     TIMESTAMP   NULL,
    created_at                  TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk__settlement__creator_id FOREIGN KEY (creator_id) REFERENCES creator(id)
)