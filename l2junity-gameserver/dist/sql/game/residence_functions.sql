CREATE TABLE IF NOT EXISTS `residence_functions` (
  `id`  int NOT NULL ,
  `level`  int NOT NULL ,
  `expiration`  bigint NOT NULL ,
  `residenceId`  int NOT NULL ,
  PRIMARY KEY (`id`, `level`, `residenceId`)
);