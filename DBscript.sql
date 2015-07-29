-- MySQL dump 10.13  Distrib 5.6.24, for Win64 (x86_64)
--
-- Host: localhost    Database: rolegame2
-- ------------------------------------------------------
-- Server version	5.6.25-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `argumento`
--

DROP TABLE IF EXISTS `argumento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `argumento` (
  `idargumento` bigint(20) NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(8000) CHARACTER SET utf8 NOT NULL,
  `name` varchar(25) CHARACTER SET latin1 NOT NULL DEFAULT '',
  `idGame` bigint(20) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`idargumento`),
  KEY `game_idx` (`idGame`),
  KEY `creator_idx` (`name`),
  CONSTRAINT `game` FOREIGN KEY (`idGame`) REFERENCES `game` (`idGame`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `player` FOREIGN KEY (`name`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `argumento`
--

LOCK TABLES `argumento` WRITE;
/*!40000 ALTER TABLE `argumento` DISABLE KEYS */;
INSERT INTO `argumento` VALUES (8,'Si juan Hello fifaifh ','juan',173),(9,'Si juan Hello anda ','juan',173),(10,'Si juan Hello anda???? ','juan',174),(11,'Si juan Hello sdff ','juan',175),(12,'Si juan Hello deeee\n ','juan',175);
/*!40000 ALTER TABLE `argumento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game` (
  `idGame` bigint(20) NOT NULL AUTO_INCREMENT,
  `nPlayers` int(11) DEFAULT NULL,
  `datecreation` timestamp NULL DEFAULT NULL,
  `datefinish` timestamp NULL DEFAULT NULL,
  `creator` varchar(25) CHARACTER SET latin1 DEFAULT NULL,
  `finalresult` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`idGame`),
  KEY `gamecreator` (`creator`),
  CONSTRAINT `gamecreator` FOREIGN KEY (`creator`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=176 DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Game';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game`
--

LOCK TABLES `game` WRITE;
/*!40000 ALTER TABLE `game` DISABLE KEYS */;
INSERT INTO `game` VALUES (140,4,'2015-07-08 20:45:01',NULL,'juan',NULL),(141,4,'2015-07-08 20:45:02',NULL,'juan',NULL),(142,4,'2015-07-08 20:45:02',NULL,'juan',NULL),(143,4,'2015-07-08 20:45:03',NULL,'juan',NULL),(144,4,'2015-07-08 20:45:04',NULL,'juan',NULL),(145,4,'2015-07-08 20:47:13',NULL,'juan',NULL),(146,4,'2015-07-08 20:47:13',NULL,'juan',NULL),(147,5,'2015-07-08 20:49:54',NULL,'juan',NULL),(148,5,'2015-07-08 20:49:57',NULL,'juan',NULL),(149,5,'2015-07-08 20:49:57',NULL,'juan',NULL),(150,5,'2015-07-08 20:49:57',NULL,'juan',NULL),(151,4,'2015-07-09 14:59:27',NULL,'juan',NULL),(152,4,'2015-07-09 14:59:36',NULL,'juan',NULL),(153,5,'2015-07-09 14:59:47',NULL,'juan',NULL),(154,4,'2015-07-09 17:43:17','2015-07-09 17:53:04','dahl','people-role'),(155,4,'2015-07-13 20:51:01','2015-07-13 21:02:13','maria','bad-role'),(156,4,'2015-07-27 18:40:34','2015-07-27 20:39:19','pedro','people-role'),(157,4,'2015-07-28 17:31:56',NULL,'juan',NULL),(158,4,'2015-07-28 18:09:08',NULL,'dahl',NULL),(159,4,'2015-07-28 19:32:05',NULL,'juan',NULL),(160,4,'2015-07-28 19:51:51',NULL,'maria',NULL),(161,4,'2015-07-28 20:23:00',NULL,'juan',NULL),(162,4,'2015-07-28 20:28:57',NULL,'juan',NULL),(163,4,'2015-07-28 23:52:00',NULL,'juan',NULL),(164,4,'2015-07-29 00:19:26',NULL,'juan',NULL),(165,4,'2015-07-29 00:55:56',NULL,'juan',NULL),(166,4,'2015-07-29 01:02:18',NULL,'juan',NULL),(167,4,'2015-07-29 01:09:45',NULL,'juan',NULL),(168,4,'2015-07-29 01:13:59',NULL,'juan',NULL),(169,4,'2015-07-29 01:35:07',NULL,'juan',NULL),(170,4,'2015-07-29 01:35:08',NULL,'juan',NULL),(171,4,'2015-07-29 01:44:44',NULL,'juan',NULL),(172,4,'2015-07-29 01:44:47',NULL,'juan',NULL),(173,4,'2015-07-29 01:49:45',NULL,'juan',NULL),(174,4,'2015-07-29 02:13:52',NULL,'juan',NULL),(175,4,'2015-07-29 16:41:12',NULL,'juan',NULL);
/*!40000 ALTER TABLE `game` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messageanalysis`
--

DROP TABLE IF EXISTS `messageanalysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messageanalysis` (
  `idUserMessage` bigint(20) NOT NULL,
  `idGame` bigint(20) NOT NULL DEFAULT '-1',
  `idRound` int(11) NOT NULL DEFAULT '-1',
  `namePlayer` varchar(25) CHARACTER SET latin1 NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '-1',
  `text` longtext CHARACTER SET latin1,
  `isArgument` varchar(25) COLLATE latin1_bin NOT NULL,
  `date` timestamp NULL DEFAULT NULL,
  `roundRole` varchar(45) COLLATE latin1_bin NOT NULL,
  `roundResult` varchar(25) COLLATE latin1_bin NOT NULL,
  `playerRole` varchar(45) COLLATE latin1_bin NOT NULL,
  `playerEliminatedBy` varchar(25) COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`idUserMessage`),
  KEY `sender` (`namePlayer`),
  KEY `msgofgame` (`idGame`),
  KEY `msgofround` (`idRound`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messageanalysis`
--

LOCK TABLES `messageanalysis` WRITE;
/*!40000 ALTER TABLE `messageanalysis` DISABLE KEYS */;
/*!40000 ALTER TABLE `messageanalysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `participation`
--

DROP TABLE IF EXISTS `participation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `participation` (
  `namePlayer` varchar(25) CHARACTER SET latin1 NOT NULL,
  `idGame` bigint(20) NOT NULL,
  `role` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `date` timestamp NULL DEFAULT NULL,
  `eliminatedBy` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `dateElimination` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`namePlayer`,`idGame`),
  KEY `playerparticipation` (`namePlayer`),
  KEY `gameparticipation` (`idGame`),
  CONSTRAINT `gameparticipation` FOREIGN KEY (`idGame`) REFERENCES `game` (`idGame`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `playerparticipation` FOREIGN KEY (`namePlayer`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `participation`
--

LOCK TABLES `participation` WRITE;
/*!40000 ALTER TABLE `participation` DISABLE KEYS */;
INSERT INTO `participation` VALUES ('dahl',151,'bad-role','2015-07-09 16:22:51',NULL,NULL),('dahl',154,'good-role','2015-07-09 17:43:27',NULL,NULL),('dahl',155,'bad-role','2015-07-13 20:51:39',NULL,NULL),('dahl',156,'doc-role','2015-07-27 18:42:48',NULL,NULL),('dahl',158,'people-role','2015-07-28 18:09:12',NULL,NULL),('juan',148,'good-role','2015-07-08 20:50:52',NULL,NULL),('juan',151,'people-role','2015-07-09 16:22:42',NULL,NULL),('juan',154,'people-role','2015-07-09 17:43:53','bad-role','2015-07-09 17:45:44'),('juan',155,'doc-role','2015-07-13 20:51:32',NULL,NULL),('juan',156,'people-role','2015-07-27 18:42:54',NULL,NULL),('juan',157,'bad-role','2015-07-28 17:42:39',NULL,NULL),('juan',159,'doc-role','2015-07-28 19:32:09',NULL,NULL),('juan',162,'doc-role','2015-07-28 20:29:00',NULL,NULL),('juan',163,'people-role','2015-07-28 23:52:04',NULL,NULL),('juan',164,'good-role','2015-07-29 00:19:29',NULL,NULL),('juan',165,'bad-role','2015-07-29 00:56:02',NULL,NULL),('juan',166,'doc-role','2015-07-29 01:02:23',NULL,NULL),('juan',167,'people-role','2015-07-29 01:09:50',NULL,NULL),('juan',168,'bad-role','2015-07-29 01:14:03',NULL,NULL),('juan',169,'bad-role','2015-07-29 01:35:11',NULL,NULL),('juan',171,'good-role','2015-07-29 01:44:51',NULL,NULL),('juan',173,'people-role','2015-07-29 01:49:49',NULL,NULL),('juan',174,'good-role','2015-07-29 02:13:56',NULL,NULL),('juan',175,'good-role','2015-07-29 16:41:18',NULL,NULL),('maria',154,'bad-role','2015-07-09 17:43:46','people-role','2015-07-09 17:53:04'),('maria',155,'people-role','2015-07-13 20:51:09','people-role','2015-07-13 21:00:02'),('maria',156,'good-role','2015-07-27 18:42:40','people-role','2015-07-27 20:36:15'),('maria',160,'doc-role','2015-07-28 19:51:56',NULL,NULL),('pedro',154,'doc-role','2015-07-09 17:43:39',NULL,NULL),('pedro',155,'good-role','2015-07-13 20:51:25','bad-role','2015-07-13 21:02:13'),('pedro',156,'bad-role','2015-07-27 18:42:30','bad-role','2015-07-27 20:39:19');
/*!40000 ALTER TABLE `participation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player` (
  `name` varchar(25) CHARACTER SET latin1 NOT NULL,
  `password` varchar(45) CHARACTER SET latin1 NOT NULL,
  `email` varchar(45) CHARACTER SET latin1 NOT NULL,
  `xp` int(11) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `sex` char(1) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Player';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player`
--

LOCK TABLES `player` WRITE;
/*!40000 ALTER TABLE `player` DISABLE KEYS */;
INSERT INTO `player` VALUES ('dahl','9633a0744a138f752ba5b290796ee936','dahl@hotmail.com',0,'1991-01-01','M'),('juan','a94652aa97c7211ba8954dd15a3cf838','j@h.com',0,'1995-06-01','M'),('maria','263bce650e68ab4e23f28263760b9fa5','maria@gmail.com',0,'2000-01-01','M'),('pedro','c6cc8094c2dc07b700ffcc36d64e2138','pedro@gmail.com',0,'1990-12-04','M');
/*!40000 ALTER TABLE `player` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `round`
--

DROP TABLE IF EXISTS `round`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `round` (
  `idRound` int(11) NOT NULL AUTO_INCREMENT,
  `idGame` bigint(20) NOT NULL DEFAULT '-1',
  `role` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `result` varchar(25) CHARACTER SET latin1 DEFAULT NULL,
  `datebeginning` timestamp NULL DEFAULT NULL,
  `datefinish` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`idRound`),
  KEY `roundofgame` (`idGame`),
  KEY `resultofvotation` (`result`),
  CONSTRAINT `resultofvotation` FOREIGN KEY (`result`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `roundofgame` FOREIGN KEY (`idGame`) REFERENCES `game` (`idGame`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=561 DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Rondas de la partida';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `round`
--

LOCK TABLES `round` WRITE;
/*!40000 ALTER TABLE `round` DISABLE KEYS */;
INSERT INTO `round` VALUES (502,143,'initial-round-role',NULL,'2015-07-08 20:45:24',NULL),(503,144,'initial-round-role',NULL,'2015-07-08 20:45:24',NULL),(504,140,'initial-round-role',NULL,'2015-07-08 20:45:24',NULL),(505,141,'initial-round-role',NULL,'2015-07-08 20:45:24',NULL),(506,142,'initial-round-role',NULL,'2015-07-08 20:45:26',NULL),(507,146,'initial-round-role',NULL,'2015-07-08 20:47:19',NULL),(508,145,'initial-round-role',NULL,'2015-07-08 20:47:19',NULL),(509,147,'initial-round-role',NULL,'2015-07-08 20:49:57',NULL),(510,149,'initial-round-role',NULL,'2015-07-08 20:50:04',NULL),(511,148,'initial-round-role',NULL,'2015-07-08 20:50:04',NULL),(512,150,'initial-round-role',NULL,'2015-07-08 20:50:04',NULL),(513,151,'initial-round-role',NULL,'2015-07-09 14:59:28',NULL),(514,152,'initial-round-role',NULL,'2015-07-09 14:59:36',NULL),(515,153,'initial-round-role',NULL,'2015-07-09 14:59:47',NULL),(516,154,'initial-round-role',NULL,'2015-07-09 17:43:18',NULL),(517,154,'bad-role','juan','2015-07-09 17:43:53','2015-07-09 17:44:57'),(518,154,'good-role','maria','2015-07-09 17:44:57','2015-07-09 17:45:16'),(519,154,'doc-role','pedro','2015-07-09 17:45:16','2015-07-09 17:45:44'),(520,154,'people-role','maria','2015-07-09 17:45:44','2015-07-09 17:50:44'),(521,154,'people-role2','maria','2015-07-09 17:50:44','2015-07-09 17:53:04'),(522,155,'initial-round-role',NULL,'2015-07-13 20:51:02',NULL),(523,155,'bad-role','maria','2015-07-13 20:51:39','2015-07-13 20:53:04'),(524,155,'good-role','maria','2015-07-13 20:53:04','2015-07-13 20:54:08'),(525,155,'doc-role','dahl','2015-07-13 20:54:09','2015-07-13 20:54:41'),(526,155,'people-role','juan','2015-07-13 20:54:42','2015-07-13 20:59:42'),(527,155,'people-role2','maria','2015-07-13 20:59:42','2015-07-13 21:00:02'),(528,155,'bad-role',NULL,'2015-07-13 21:00:02',NULL),(529,155,'bad-role',NULL,'2015-07-13 21:00:02',NULL),(530,155,'bad-role','pedro','2015-07-13 21:00:02','2015-07-13 21:01:33'),(531,155,'good-role','maria','2015-07-13 21:01:33','2015-07-13 21:01:53'),(532,155,'doc-role','maria','2015-07-13 21:01:53','2015-07-13 21:02:13'),(533,156,'initial-round-role',NULL,'2015-07-27 18:40:34',NULL),(534,156,'bad-role','juan','2015-07-27 18:42:54','2015-07-27 18:44:24'),(535,156,'good-role','pedro','2015-07-27 18:44:24','2015-07-27 18:45:54'),(536,156,'doc-role','juan','2015-07-27 18:45:54','2015-07-27 18:47:25'),(537,156,'people-role','dahl','2015-07-27 18:47:25','2015-07-27 18:52:25'),(538,156,'people-role2','maria','2015-07-27 18:52:25','2015-07-27 20:36:08'),(539,156,'bad-role','pedro','2015-07-27 20:36:17','2015-07-27 20:37:49'),(540,156,'good-role',NULL,'2015-07-27 20:37:49',NULL),(541,156,'doc-role','dahl','2015-07-27 20:37:49','2015-07-27 20:39:19'),(542,157,'initial-round-role',NULL,'2015-07-28 17:31:57',NULL),(543,158,'initial-round-role',NULL,'2015-07-28 18:09:09',NULL),(544,159,'initial-round-role',NULL,'2015-07-28 19:32:05',NULL),(545,160,'initial-round-role',NULL,'2015-07-28 19:51:52',NULL),(546,161,'initial-round-role',NULL,'2015-07-28 20:23:01',NULL),(547,162,'initial-round-role',NULL,'2015-07-28 20:28:57',NULL),(548,163,'initial-round-role',NULL,'2015-07-28 23:52:01',NULL),(549,164,'initial-round-role',NULL,'2015-07-29 00:19:27',NULL),(550,165,'initial-round-role',NULL,'2015-07-29 00:55:57',NULL),(551,166,'initial-round-role',NULL,'2015-07-29 01:02:18',NULL),(552,167,'initial-round-role',NULL,'2015-07-29 01:09:45',NULL),(553,168,'initial-round-role',NULL,'2015-07-29 01:13:59',NULL),(554,169,'initial-round-role',NULL,'2015-07-29 01:35:07',NULL),(555,170,'initial-round-role',NULL,'2015-07-29 01:35:08',NULL),(556,171,'initial-round-role',NULL,'2015-07-29 01:44:45',NULL),(557,172,'initial-round-role',NULL,'2015-07-29 01:44:47',NULL),(558,173,'initial-round-role',NULL,'2015-07-29 01:49:45',NULL),(559,174,'initial-round-role',NULL,'2015-07-29 02:13:52',NULL),(560,175,'initial-round-role',NULL,'2015-07-29 16:41:12',NULL);
/*!40000 ALTER TABLE `round` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `session` (
  `idSession` bigint(20) NOT NULL AUTO_INCREMENT,
  `namePlayer` varchar(25) CHARACTER SET latin1 DEFAULT NULL,
  `login` timestamp NULL DEFAULT NULL,
  `logout` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`idSession`),
  KEY `playerinsession` (`namePlayer`),
  CONSTRAINT `playerinsession` FOREIGN KEY (`namePlayer`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=646 DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session`
--

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` VALUES (594,'juan','2015-07-08 20:44:07',NULL),(595,'juan','2015-07-08 20:49:28',NULL),(596,'juan','2015-07-09 14:59:00','2015-07-09 15:00:00'),(597,'dahl','2015-07-09 15:01:08',NULL),(598,'juan','2015-07-09 15:54:31',NULL),(599,'maria','2015-07-09 16:20:18',NULL),(600,'juan','2015-07-09 16:20:41',NULL),(601,'juan','2015-07-09 17:09:00',NULL),(602,'dahl','2015-07-09 17:09:59',NULL),(603,'juan','2015-07-09 17:36:41','2015-07-09 17:38:42'),(604,'dahl','2015-07-09 17:36:43',NULL),(605,'maria','2015-07-09 17:37:01',NULL),(606,'pedro','2015-07-09 17:38:03',NULL),(607,'juan','2015-07-09 17:39:22','2015-07-09 17:39:30'),(608,'juan','2015-07-09 17:42:47',NULL),(609,'maria','2015-07-09 17:42:53',NULL),(610,'pedro','2015-07-09 17:43:03',NULL),(611,'dahl','2015-07-09 17:43:11',NULL),(612,'dahl','2015-07-13 20:49:40',NULL),(613,'juan','2015-07-13 20:50:01',NULL),(614,'pedro','2015-07-13 20:50:26',NULL),(615,'maria','2015-07-13 20:50:49',NULL),(616,'juan','2015-07-27 18:38:53',NULL),(617,'dahl','2015-07-27 18:39:41',NULL),(618,'maria','2015-07-27 18:40:00',NULL),(619,'pedro','2015-07-27 18:40:23',NULL),(620,'juan','2015-07-28 17:31:32','2015-07-28 17:41:23'),(621,'juan','2015-07-28 17:42:32',NULL),(622,'dahl','2015-07-28 18:05:32',NULL),(623,'dahl','2015-07-28 18:09:00',NULL),(624,'juan','2015-07-28 18:38:47',NULL),(625,'juan','2015-07-28 19:31:54','2015-07-28 19:47:22'),(626,'juan','2015-07-28 19:49:05',NULL),(627,'dahl','2015-07-28 19:49:33',NULL),(628,'maria','2015-07-28 19:51:47',NULL),(629,'dahl','2015-07-28 20:21:25',NULL),(630,'juan','2015-07-28 20:22:57','2015-07-28 20:26:21'),(631,'juan','2015-07-28 20:28:54',NULL),(632,'juan','2015-07-28 23:51:56',NULL),(633,'juan','2015-07-29 00:19:23',NULL),(634,'dahl','2015-07-29 00:41:14',NULL),(635,'juan','2015-07-29 00:55:51',NULL),(636,'juan','2015-07-29 01:02:14',NULL),(637,'dahl','2015-07-29 01:07:50',NULL),(638,'juan','2015-07-29 01:09:39',NULL),(639,'juan','2015-07-29 01:13:55',NULL),(640,'juan','2015-07-29 01:35:02',NULL),(641,'juan','2015-07-29 01:44:40',NULL),(642,'juan','2015-07-29 01:49:39',NULL),(643,'dahl','2015-07-29 02:12:31',NULL),(644,'juan','2015-07-29 02:13:47',NULL),(645,'juan','2015-07-29 16:41:06',NULL);
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tkivalues`
--

DROP TABLE IF EXISTS `tkivalues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tkivalues` (
  `namePlayer` varchar(25) NOT NULL,
  `Competidor_Val` int(11) NOT NULL,
  `Competidor_Perc` int(11) NOT NULL,
  `Competidor_Cat` varchar(5) NOT NULL,
  `Colaborador_Val` int(11) NOT NULL,
  `Colaborador_Perc` int(11) NOT NULL,
  `Colaborador_Cat` varchar(5) NOT NULL,
  `Compromiso_Val` int(11) NOT NULL,
  `Compromiso_Perc` int(11) NOT NULL,
  `Compromiso_Cat` varchar(5) NOT NULL,
  `Eludir_Val` int(11) NOT NULL,
  `Eludir_Perc` int(11) NOT NULL,
  `Eludir_Cat` varchar(5) NOT NULL,
  `Complaciente_Val` int(11) NOT NULL,
  `Complaciente_Perc` int(11) NOT NULL,
  `Complaciente_Cat` varchar(5) NOT NULL,
  `MainCategory` varchar(25) NOT NULL,
  PRIMARY KEY (`namePlayer`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tkivalues`
--

LOCK TABLES `tkivalues` WRITE;
/*!40000 ALTER TABLE `tkivalues` DISABLE KEYS */;
/*!40000 ALTER TABLE `tkivalues` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usermessage`
--

DROP TABLE IF EXISTS `usermessage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usermessage` (
  `idUserMessage` bigint(20) NOT NULL AUTO_INCREMENT,
  `idGame` bigint(20) NOT NULL DEFAULT '-1',
  `idRound` int(11) NOT NULL DEFAULT '-1',
  `namePlayer` varchar(25) CHARACTER SET latin1 NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '-1',
  `text` longtext CHARACTER SET latin1,
  `date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`idUserMessage`),
  KEY `sender` (`namePlayer`),
  KEY `msgofgame` (`idGame`),
  KEY `msgofround` (`idRound`),
  CONSTRAINT `msgofgame` FOREIGN KEY (`idGame`) REFERENCES `round` (`idGame`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `msgofround` FOREIGN KEY (`idRound`) REFERENCES `round` (`idRound`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sender` FOREIGN KEY (`namePlayer`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1594 DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usermessage`
--

LOCK TABLES `usermessage` WRITE;
/*!40000 ALTER TABLE `usermessage` DISABLE KEYS */;
INSERT INTO `usermessage` VALUES (1587,148,511,'juan',2,'hola','2015-07-08 20:54:40'),(1588,148,511,'juan',2,'hola','2015-07-08 20:55:15'),(1589,151,513,'juan',2,'hola','2015-07-09 16:44:58'),(1590,155,523,'dahl',2,'hola','2015-07-13 20:51:53'),(1591,155,524,'pedro',2,'hola','2015-07-13 20:53:29'),(1592,157,542,'juan',2,'kk','2015-07-28 17:33:43'),(1593,166,551,'juan',2,'vvxv','2015-07-29 01:03:57');
/*!40000 ALTER TABLE `usermessage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vote`
--

DROP TABLE IF EXISTS `vote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vote` (
  `idVote` bigint(20) NOT NULL AUTO_INCREMENT,
  `fromPlayer` varchar(25) CHARACTER SET latin1 DEFAULT NULL,
  `toPlayer` varchar(25) CHARACTER SET latin1 DEFAULT NULL,
  `idRound` int(11) NOT NULL DEFAULT '-1',
  `idGame` bigint(20) NOT NULL DEFAULT '-1',
  `auto` tinyint(1) DEFAULT NULL,
  `date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`idVote`),
  KEY `votefrom` (`fromPlayer`),
  KEY `voteto` (`toPlayer`),
  KEY `voteinround` (`idRound`),
  KEY `voteingame` (`idGame`),
  CONSTRAINT `votefrom` FOREIGN KEY (`fromPlayer`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `voteingame` FOREIGN KEY (`idGame`) REFERENCES `round` (`idGame`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `voteinround` FOREIGN KEY (`idRound`) REFERENCES `round` (`idRound`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `voteto` FOREIGN KEY (`toPlayer`) REFERENCES `player` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=800 DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vote`
--

LOCK TABLES `vote` WRITE;
/*!40000 ALTER TABLE `vote` DISABLE KEYS */;
INSERT INTO `vote` VALUES (781,'maria','juan',517,154,0,'2015-07-09 17:44:57'),(782,'dahl','maria',518,154,0,'2015-07-09 17:45:16'),(783,'pedro','pedro',519,154,0,'2015-07-09 17:45:44'),(784,'maria','pedro',521,154,1,'2015-07-09 17:51:04'),(785,'pedro','dahl',521,154,1,'2015-07-09 17:51:04'),(786,'dahl','juan',521,154,1,'2015-07-09 17:51:04'),(787,'dahl','maria',523,155,0,'2015-07-13 20:53:03'),(788,'pedro','maria',524,155,0,'2015-07-13 20:54:08'),(789,'juan','dahl',525,155,0,'2015-07-13 20:54:41'),(790,'dahl','juan',526,155,0,'2015-07-13 20:55:14'),(791,'dahl','juan',527,155,1,'2015-07-13 21:00:02'),(792,'juan','maria',527,155,1,'2015-07-13 21:00:02'),(793,'pedro','maria',527,155,1,'2015-07-13 21:00:02'),(794,'dahl','pedro',530,155,1,'2015-07-13 21:00:22'),(795,'dahl','juan',530,155,1,'2015-07-13 21:00:22'),(796,'dahl','pedro',530,155,1,'2015-07-13 21:00:22'),(797,'pedro','maria',531,155,1,'2015-07-13 21:01:53'),(798,'juan','maria',532,155,1,'2015-07-13 21:02:13'),(799,'juan','maria',538,156,0,'2015-07-27 18:56:52');
/*!40000 ALTER TABLE `vote` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-07-29 13:43:26
