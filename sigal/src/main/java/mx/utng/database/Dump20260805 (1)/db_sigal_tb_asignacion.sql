-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: db_sigal
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `tb_asignacion`
--

DROP TABLE IF EXISTS `tb_asignacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asignacion` (
  `ID_Asignacion` int(11) NOT NULL AUTO_INCREMENT,
  `TipoUsuario` enum('Profesor','Administrativo','Alumno','Otro') NOT NULL,
  `NombreSolicitante` varchar(100) DEFAULT NULL,
  `Materia` varchar(100) DEFAULT NULL,
  `Grupo` varchar(20) DEFAULT NULL,
  `NumAlumnos` int(11) DEFAULT NULL,
  `Fecha` date NOT NULL,
  `HoraInicio` time NOT NULL,
  `HoraTermino` time NOT NULL,
  `Actividad` varchar(255) DEFAULT NULL,
  `Estado` enum('Libre','Ocupado','Reservado','Cancelado') NOT NULL,
  `JustificacionCancelacion` varchar(255) DEFAULT NULL,
  `OtraCarrera` varchar(100) DEFAULT NULL,
  `ID_Carrera` int(11) DEFAULT NULL,
  `ID_Profesor` int(11) DEFAULT NULL,
  `ID_Usuario` int(11) NOT NULL,
  `ID_Espacio` int(11) NOT NULL,
  PRIMARY KEY (`ID_Asignacion`),
  KEY `ID_Carrera` (`ID_Carrera`),
  KEY `ID_Profesor` (`ID_Profesor`),
  KEY `ID_Usuario` (`ID_Usuario`),
  KEY `ID_Espacio` (`ID_Espacio`),
  CONSTRAINT `tb_asignacion_ibfk_1` FOREIGN KEY (`ID_Carrera`) REFERENCES `tb_carrera` (`ID_Carrera`),
  CONSTRAINT `tb_asignacion_ibfk_2` FOREIGN KEY (`ID_Profesor`) REFERENCES `tb_profesor` (`ID_Profesor`),
  CONSTRAINT `tb_asignacion_ibfk_3` FOREIGN KEY (`ID_Usuario`) REFERENCES `tb_usuario` (`ID_Usuario`),
  CONSTRAINT `tb_asignacion_ibfk_4` FOREIGN KEY (`ID_Espacio`) REFERENCES `tb_espacio` (`ID_Espacio`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_asignacion`
--

LOCK TABLES `tb_asignacion` WRITE;
/*!40000 ALTER TABLE `tb_asignacion` DISABLE KEYS */;
INSERT INTO `tb_asignacion` VALUES (2,'Profesor','mar','poo','GTID235',18,'2026-08-06','10:00:00','11:50:00','Practica','Reservado',NULL,'Desarrlollo',NULL,NULL,1,3),(3,'Profesor','maria','poo','GTID235',16,'2026-08-12','09:00:00','10:50:00','practica 3','Reservado',NULL,'desarrollo',NULL,NULL,1,4);
/*!40000 ALTER TABLE `tb_asignacion` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-05 15:17:31
