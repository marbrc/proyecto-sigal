-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: db_sigal
-- ------------------------------------------------------
-- Server version	8.0.46

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
  `ID_Asignacion` int NOT NULL AUTO_INCREMENT,
  `NombreSolicitante` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ID_Materia` int DEFAULT NULL,
  `NumAlumnos` int DEFAULT NULL,
  `Fecha` date NOT NULL,
  `HoraInicio` time NOT NULL,
  `HoraTermino` time NOT NULL,
  `Actividad` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `Estado` enum('Libre','Ocupado','Asignado','Cancelado') COLLATE utf8mb4_general_ci NOT NULL,
  `JustificacionCancelacion` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `OtraCarrera` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ID_Carrera` int DEFAULT NULL,
  `ID_Grupo` int DEFAULT NULL,
  `OtroGrupo` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ID_Profesor` int DEFAULT NULL,
  `ID_Usuario` int NOT NULL,
  `ID_Espacio` int NOT NULL,
  `Grupo` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `TipoUsuario` enum('Docente','Administrativo') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `Materia` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `Carrera` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`ID_Asignacion`),
  KEY `ID_Carrera` (`ID_Carrera`),
  KEY `ID_Profesor` (`ID_Profesor`),
  KEY `ID_Usuario` (`ID_Usuario`),
  KEY `ID_Espacio` (`ID_Espacio`),
  KEY `ID_Grupo` (`ID_Grupo`),
  KEY `tb_asignacion_ibfk_6` (`ID_Materia`),
  CONSTRAINT `tb_asignacion_ibfk_1` FOREIGN KEY (`ID_Carrera`) REFERENCES `tb_carrera` (`ID_Carrera`),
  CONSTRAINT `tb_asignacion_ibfk_2` FOREIGN KEY (`ID_Profesor`) REFERENCES `tb_profesor` (`ID_Profesor`),
  CONSTRAINT `tb_asignacion_ibfk_3` FOREIGN KEY (`ID_Usuario`) REFERENCES `tb_usuario` (`ID_Usuario`),
  CONSTRAINT `tb_asignacion_ibfk_4` FOREIGN KEY (`ID_Espacio`) REFERENCES `tb_espacio` (`ID_Espacio`),
  CONSTRAINT `tb_asignacion_ibfk_5` FOREIGN KEY (`ID_Grupo`) REFERENCES `tb_grupo` (`ID_Grupo`),
  CONSTRAINT `tb_asignacion_ibfk_6` FOREIGN KEY (`ID_Materia`) REFERENCES `tb_materia` (`ID_Materia`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_asignacion`
--

LOCK TABLES `tb_asignacion` WRITE;
/*!40000 ALTER TABLE `tb_asignacion` DISABLE KEYS */;
INSERT INTO `tb_asignacion` VALUES (2,'mar',NULL,18,'2026-08-06','10:00:00','11:50:00','Practica','Cancelado',NULL,'Desarrlollo',NULL,NULL,NULL,NULL,1,3,NULL,NULL,NULL,NULL),(4,'mar',NULL,13,'2026-08-13','10:00:00','11:50:00','wert','Asignado',NULL,'mae',NULL,NULL,NULL,NULL,1,2,NULL,NULL,NULL,NULL),(5,'mar',NULL,NULL,'2026-08-11','10:00:00','10:50:00',NULL,'Asignado',NULL,NULL,NULL,NULL,NULL,NULL,1,12,NULL,NULL,NULL,NULL),(6,'Javier',NULL,16,'2026-08-14','09:00:00','09:50:00','R1','Asignado',NULL,'Mercadotecnia',NULL,NULL,NULL,1,3,10,NULL,NULL,NULL,NULL),(7,'Maria',NULL,12,'2026-08-14','09:00:00','09:50:00','r2','Asignado',NULL,NULL,NULL,NULL,NULL,NULL,3,10,NULL,NULL,NULL,NULL),(8,'Maria',NULL,12,'2026-08-21','07:30:00','08:30:00','dgdfsdd','Asignado',NULL,NULL,NULL,NULL,NULL,NULL,1,13,'GTID_235','Docente','Administración de Servidores','Licenciatura en Ingeniería en Tecnologías de la Información e Innovación Digital – Entornos Virtuales y Negocios Digitales');
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

-- Dump completed on 2026-08-18 17:31:59
