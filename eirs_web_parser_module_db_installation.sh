#!bin/bash
conffile=/u01/eirsapp/configuration/configuration.properties
typeset -A config # init array

while read line
do
    if echo $line | grep -F = &>/dev/null
    then
        varname=$(echo "$line" | cut -d '=' -f 1)
        config[$varname]=$(echo "$line" | cut -d '=' -f 2-)
    fi
done < $conffile
conn1="mysql -h${config[ip]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]}"
conn="mysql -h${config[ip]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]} ${config[appdbName]}"

echo "creating apptest database."
${conn1} -e "CREATE DATABASE IF NOT EXISTS apptest;"
echo "apptest database successfully created!"

`${conn} <<EOFMYSQL
CREATE TABLE if not exists trc_local_manufactured_devices_dump (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  imei varchar(20) DEFAULT NULL,
  serial_number varchar(15) DEFAULT NULL,
  manufacturer_id varchar(20) DEFAULT NULL,
  manufacturer_name varchar(50) DEFAULT NULL,
  manufacturering_date varchar(15) DEFAULT NULL,
  tac varchar(8) DEFAULT NULL,
  actual_imei  varchar(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY createdOn_idx (created_on),
  KEY imei_idx (imei)
);
CREATE TABLE if not exists trc_type_approved_data (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  no int DEFAULT NULL,
  company varchar(100) DEFAULT NULL,
  trademark varchar(50) DEFAULT NULL,
  product_name varchar(50) DEFAULT NULL,
  model varchar(50) DEFAULT NULL,
  country varchar(50) DEFAULT NULL,
  tx_frequency varchar(50) DEFAULT NULL,
  rx_frequency varchar(50) DEFAULT NULL,
  trc_identifier varchar(50) DEFAULT NULL,
  type_of_equipment varchar(50) DEFAULT NULL,
  approval_date varchar(15) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY model (model)
);

CREATE TABLE if not exists trc_qualified_agents_data (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  no int DEFAULT NULL,
  company_name varchar(100) DEFAULT NULL,
  phone_number varchar(50) DEFAULT NULL,
  email varchar(50) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY createdOn_idx (created_on)
);

CREATE TABLE if not exists sys_param (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description varchar(255) DEFAULT '',
  tag varchar(255) DEFAULT NULL,
  type int DEFAULT '0',
  value text,
  active int DEFAULT '0',
  feature_name varchar(255) DEFAULT '',
  remark varchar(255) DEFAULT '',
  user_type varchar(255) DEFAULT '',
  modified_by varchar(255) DEFAULT '',
  PRIMARY KEY (id),
  UNIQUE KEY tag (tag)
);

CREATE TABLE if not exists sys_generated_alert (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  alert_id varchar(20) DEFAULT '',
  description varchar(250) DEFAULT '',
  status int DEFAULT '0',
  user_id int DEFAULT '0',
  username varchar(50) DEFAULT '',
  PRIMARY KEY (id)
);

CREATE TABLE if not exists cfg_feature_alert (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  alert_id varchar(20) DEFAULT '',
  description varchar(250) DEFAULT '',
  feature varchar(250) DEFAULT '',
  PRIMARY KEY (id)
);

CREATE TABLE if not exists rule (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  description varchar(2500) DEFAULT '',
  name varchar(50) DEFAULT '',
  output varchar(5) DEFAULT '',
  state varchar(20) DEFAULT '',
  modified_by varchar(30) DEFAULT '',
  PRIMARY KEY (id)
);

CREATE TABLE if not exists feature_rule (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  feature varchar(20) DEFAULT '',
  grace_action varchar(20) DEFAULT '',
  name varchar(255) DEFAULT '',
  post_grace_action varchar(20) DEFAULT '',
  rule_order int DEFAULT '0',
  user_type varchar(20) DEFAULT '',
  failed_rule_action_grace varchar(10) DEFAULT '',
  failed_rule_action_post_grace varchar(10) DEFAULT '',
  output varchar(5) DEFAULT '',
  rule_message varchar(1000) DEFAULT '',
  modified_by varchar(50) DEFAULT '',
  PRIMARY KEY (id)
);

CREATE TABLE if not exists trc_data_mgmt (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  file_name varchar(250) DEFAULT NULL,
  status varchar(20) DEFAULT NULL,
  transaction_id varchar(20) DEFAULT NULL,
  user_id varchar(50) DEFAULT NULL,
  request_type varchar(20) DEFAULT NULL,
  remarks varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE if not exists web_action_db (
  id int NOT NULL AUTO_INCREMENT,
  created_on datetime DEFAULT NULL,
  data varchar(255) DEFAULT NULL,
  feature varchar(255) DEFAULT NULL,
  modified_on datetime DEFAULT NULL,
  state int NOT NULL,
  sub_feature varchar(255) DEFAULT NULL,
  txn_id varchar(255) DEFAULT NULL,
  retry_count int DEFAULT '0',
  PRIMARY KEY (id)
);

insert into sys_param (description, tag, value, feature_name) SELECT 'The maximum value record count allowed to upload in the local manufacturer file.', 'LM_FILE_COUNT', '1000', 'TRC Web Parser' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'LM_FILE_COUNT');
insert into feature_rule (feature, grace_action, name, post_grace_action, rule_order, user_type, failed_rule_action_grace, failed_rule_action_post_grace, output, rule_message) values ('LM',
'Skip', 'EXISTS_IN_LOCAL_MANUFACTURER_DB', 'Skip', 1, 'TRC', 'Record', 'Record', 'No', 'The imei is present in local manufacturer db.');
insert into feature_rule (feature, grace_action, name, post_grace_action, rule_order, user_type, failed_rule_action_grace, failed_rule_action_post_grace, output, rule_message) values ('LM',
'Skip', 'EXISTS_IN_GSMA_DB_TYPE_APPROVED', 'Skip', 2, 'TRC', 'Record', 'Record', 'Yes', 'The tac is not type approved in mdr db.');
insert into feature_rule (feature, grace_action, name, post_grace_action, rule_order, user_type, failed_rule_action_grace, failed_rule_action_post_grace, output, rule_message) values ('LM',
'Skip', 'EXISTS_IN_GSMA_DETAILS_DB', 'Skip', 3, 'TRC', 'Record', 'Record', 'Yes', 'The tac is not present in mdr db.');
 insert into feature_rule (feature, grace_action, name, post_grace_action, rule_order, user_type, failed_rule_action_grace, failed_rule_action_post_grace, output, rule_message) values ('LM',
'Skip', 'EXISTS_IN_DUPLICATE_DB', 'Skip', 4, 'TRC', 'Record', 'Record', 'No', 'The imei is present in duplicate db.');
insert into feature_rule (feature, grace_action, name, post_grace_action, rule_order, user_type, failed_rule_action_grace, failed_rule_action_post_grace, output, rule_message) values ('LM',
'Skip', 'EXISTS_IN_STOLEN_DB', 'Skip', 5, 'TRC', 'Record', 'Record', 'No', 'The imei is present in stolen db.');
insert into feature_rule (feature, grace_action, name, post_grace_action, rule_order, user_type, failed_rule_action_grace, failed_rule_action_post_grace, output, rule_message) values ('LM',
'Skip', 'NATIONAL_WHITELISTS', 'Skip', 6, 'TRC', 'Record', 'Record', 'No', 'The imei is present in national whitelist.');

insert into rule (description, name, output, state) values ('Checks if imei present in stolen db or not', 'EXISTS_IN_STOLEN_DB', 'Yes', 'Enabled');
insert into rule (description, name, output, state) values ('Checks if imei present in duplicate db or not', 'EXISTS_IN_DUPLICATE_DB', 'Yes', 'Enabled');
insert into rule (description, name, output, state) values ('Checks if tac is type approved in mdr db or not', 'EXISTS_IN_GSMA_DB_TYPE_APPROVED', 'Yes', 'Enabled');
insert into rule (description, name, output, state) values ('Checks if imei present in local manufacturer db or not', 'EXISTS_IN_LOCAL_MANUFACTURER_DB', 'Yes', 'Enabled');
insert into rule (description, name, output, state) values ('Checks if imei present in national whitelist db or not', 'NATIONAL_WHITELISTS', 'Yes', 'Enabled');

insert into cfg_feature_alert (alert_id, description, feature) values ('alert6001', 'File <process_name> for <e> is not available', 'Web Parser');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert6002', 'File <process_name > for TRC <e> contains data in invalid format', 'Web Parser');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert6003', 'Error : <e> for file <process_name>', 'Web Parser');


EOFMYSQL`

echo "Tables creation completed."
echo "                                             *
						  ***
						 *****
						  ***
						   *                           "
echo "********************Thank You DB Process is completed now for Web Parser Module*****************"