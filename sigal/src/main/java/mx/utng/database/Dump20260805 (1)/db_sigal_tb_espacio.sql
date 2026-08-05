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
-- Table structure for table `tb_espacio`
--

DROP TABLE IF EXISTS `tb_espacio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_espacio` (
  `ID_Espacio` int(11) NOT NULL AUTO_INCREMENT,
  `ClaveEspacio` varchar(20) NOT NULL,
  `NombreEspacio` varchar(50) NOT NULL,
  `TipoEspacio` enum('Aula común','Lab. de cómputo','Especializado','Sala múltiple') NOT NULL,
  `CapacidadMaxima` int(11) NOT NULL,
  `Estado` enum('Disponible','En mantenimiento','Fuera de servicio') NOT NULL,
  `Descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_Espacio`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_espacio`
--

LOCK TABLES `tb_espacio` WRITE;
/*!40000 ALTER TABLE `tb_espacio` DISABLE KEYS */;
INSERT INTO `tb_espacio` VALUES (1,'ATI-01','Aula TI - 1','Aula común',30,'Disponible','Planta alta'),(2,'ATI-02','Aula TI - 2','Aula común',30,'Disponible','Planta alta'),(3,'ATI-03','Aula TI - 3','Aula común',30,'Disponible','Planta alta'),(4,'ATI-04','Aula TI - 4','Aula común',30,'Disponible','Planta alta'),(5,'ATI-05','Aula TI - 5','Aula común',30,'Disponible','Planta alta'),(6,'ATI-06','Aula TI - 6','Aula común',30,'Disponible','Planta alta'),(7,'ATI-07','Aula TI - 7','Aula común',30,'Disponible','Planta alta'),(8,'ATI-08','Aula TI - 8','Aula común',30,'Disponible','Planta alta'),(9,'ATI-09','Aula TI - 9','Aula común',30,'Disponible','Planta alta'),(10,'ATI-10','Aula TI - 10','Aula común',30,'Disponible','Planta alta'),(11,'ATI-11','Aula TI - 11','Aula común',30,'Disponible','Planta alta'),(12,'ATI-12','Aula TI - 12','Aula común',30,'Disponible','Planta alta'),(13,'LAB-01','LAB TI - I','Lab. de cómputo',24,'Disponible','Laboratorio de cómputo general - Planta alta'),(14,'LAB-02','LAB TI - II','Lab. de cómputo',24,'Disponible','Laboratorio de cómputo general - Planta alta'),(15,'LAB-03','LAB TI - III','Lab. de cómputo',24,'Disponible','Laboratorio de cómputo general - Planta alta'),(16,'LAB-04','LAB TI - IV','Lab. de cómputo',24,'Disponible','Laboratorio de cómputo general - Planta alta'),(17,'SUM-01','Sala de Juntas y de Capacitación','Sala múltiple',35,'Disponible','Planta alta'),(18,'ESP-01','Taller de Soporte, Corte y Modelado','Especializado',20,'Disponible','Planta alta'),(19,'SUM-02','Sala Audiovisual','Sala múltiple',40,'Disponible','Planta baja'),(20,'ESP-02','Laboratorio de Seguridad','Especializado',24,'Disponible','Planta baja'),(21,'ESP-03','Laboratorio WAN','Especializado',24,'Disponible','Planta baja'),(22,'ESP-04','Taller de Dibujo y Pintura','Especializado',30,'Disponible','Planta baja'),(23,'ESP-05','Laboratorio de Fotografía','Especializado',20,'Disponible','Planta baja'),(24,'SUM-03','Sala de Reuniones','Sala múltiple',20,'Disponible','Planta alta');
/*!40000 ALTER TABLE `tb_espacio` ENABLE KEYS */;
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
