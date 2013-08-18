
create table batch_job_entity (
	batch_job_entity_id bigint identity not null primary key,
	version bigint,
	job_name varchar(500) not null,
	job_incrementer blob
);