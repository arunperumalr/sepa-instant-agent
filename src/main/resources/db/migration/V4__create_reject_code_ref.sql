CREATE TABLE reject_code_ref
(
    code          VARCHAR(10)  NOT NULL,
    title         VARCHAR(100) NOT NULL,
    description   VARCHAR(500) NOT NULL,
    common_cause  VARCHAR(500) NOT NULL,
    action_needed VARCHAR(500) NOT NULL,
    action_owner  VARCHAR(10)  NOT NULL,

    PRIMARY KEY (code),
    CONSTRAINT ck_reject_owner CHECK (action_owner IN ('CUSTOMER', 'BANK', 'INTERNAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO reject_code_ref (code, title, description, common_cause, action_needed, action_owner)
VALUES ('AC01', 'Invalid account number (IBAN)',
        'The beneficiary account number is incorrect.',
        'The IBAN is malformed, fails check-digit validation, or does not exist.',
        'Verify the account number with the customer and ask for the correct IBAN.',
        'CUSTOMER'),
       ('AC04', 'Account closed',
        'The beneficiary account has been closed.',
        'The account was closed on the receiving bank''s books.',
        'Request updated account details from the beneficiary.',
        'CUSTOMER'),
       ('AC06', 'Account blocked',
        'The beneficiary account is blocked for this type of payment.',
        'The receiving bank has blocked the account.',
        'Ask the customer to contact the receiving bank or use a different account.',
        'CUSTOMER'),
       ('AG01', 'Transaction forbidden',
        'This type of payment is not allowed on the beneficiary account.',
        'The account type does not accept this payment.',
        'Ask the customer to confirm the account type or use a different account.',
        'CUSTOMER'),
       ('AM04', 'Insufficient funds',
        'The account used for the payment has insufficient funds.',
        'Not enough balance at the time of the payment.',
        'Confirm the balance, then retry or use another funding source.',
        'CUSTOMER'),
       ('MS03', 'Reason not specified',
        'The bank rejected the payment without giving a specific reason.',
        'The receiving bank did not supply a detailed code.',
        'Escalate to the payments operations team to ask the bank for the reason.',
        'INTERNAL');