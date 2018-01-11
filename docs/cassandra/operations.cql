CREATE KEYSPACE ocds
WITH replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 3
};


CREATE TABLE ocds.operations (
id uuid,
platform_id uuid,
PRIMARY KEY (id)
);
