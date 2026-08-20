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
-- Table structure for table `tb_materia`
--

DROP TABLE IF EXISTS `tb_materia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_materia` (
  `ID_Materia` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `Descripcion` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`ID_Materia`),
  UNIQUE KEY `Nombre` (`Nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_materia`
--

LOCK TABLES `tb_materia` WRITE;
/*!40000 ALTER TABLE `tb_materia` DISABLE KEYS */;
INSERT INTO `tb_materia` VALUES (1,'Administración de Servidores',NULL),(2,'Animación 2D',NULL),(3,'Animación 2D Avanzada',NULL),(4,'Animación 3D',NULL),(5,'Animación de Personajes 3D',NULL),(6,'Análisis y Diseño de Software',NULL),(7,'Aplicaciones Web',NULL),(8,'Aplicaciones Web Orientadas a Servicios',NULL),(9,'Aplicaciones para Realidad Aumentada',NULL),(10,'Aplicaciones para Realidad Virtual',NULL),(11,'Bases de Datos',NULL),(12,'Bases de Datos Avanzadas',NULL),(13,'Centro de Datos',NULL),(14,'Ciencia de Datos',NULL),(15,'Composición y Efectos Visuales',NULL),(16,'Comunicación Gráfica',NULL),(17,'Comunicación y Habilidades Digitales',NULL),(18,'Conexión de Redes WAN',NULL),(19,'Conmutación y Enrutamiento de Redes',NULL),(20,'Costos',NULL),(21,'Cálculo Diferencial',NULL),(22,'Cálculo Integral',NULL),(23,'Cálculo de Varias Variables',NULL),(24,'Cómputo en la Nube',NULL),(25,'Desarrollo Humano y Valores',NULL),(26,'Desarrollo de Aplicaciones Móviles',NULL),(27,'Desarrollo de Personajes 3D',NULL),(28,'Desarrollo del Pensamiento y Toma de Decisiones',NULL),(29,'Diseño Digital y Producción Audiovisual',NULL),(30,'Diseño Editorial y Publicidad Digital',NULL),(31,'Diseño Sonoro',NULL),(32,'Diseño de Interfaz Gráfica',NULL),(33,'Ecuaciones Diferenciales',NULL),(34,'Edición de Audio y Video',NULL),(35,'Electrónica Digital',NULL),(36,'Escalabilidad de Redes',NULL),(37,'Estrategias Publicitarias',NULL),(38,'Estructura de Datos',NULL),(39,'Estándares y Métricas para el Desarrollo de Software',NULL),(40,'Evaluación de Proyectos de Tecnología',NULL),(41,'Formulación de Proyectos de Tecnología',NULL),(42,'Fotografía y Video',NULL),(43,'Frameworks para Desarrollo Web',NULL),(44,'Fundamentos Matemáticos',NULL),(45,'Fundamentos de Diseño y del Lenguaje Visual',NULL),(46,'Fundamentos de Inteligencia Artificial',NULL),(47,'Fundamentos de Programación',NULL),(48,'Fundamentos de Redes',NULL),(49,'Física',NULL),(50,'Geometría Descriptiva',NULL),(51,'Gestión Creativa',NULL),(52,'Gestión de Proyectos',NULL),(53,'Gestión de Proyectos de Tecnología',NULL),(54,'Guionismo y Storyboard',NULL),(55,'Habilidades Gerenciales',NULL),(56,'Habilidades Socioemocionales y Manejo de Conflictos',NULL),(57,'Identidad Visual',NULL),(58,'Ilustración Digital',NULL),(59,'Ilustración para Animación',NULL),(60,'Imagen Digital Animada',NULL),(61,'Informática Forense',NULL),(62,'Infraestructura de Redes de Datos',NULL),(63,'Inglés I',NULL),(64,'Inglés II',NULL),(65,'Inglés III',NULL),(66,'Inglés IV',NULL),(67,'Inglés V',NULL),(68,'Inglés VI',NULL),(69,'Inglés VII',NULL),(70,'Inglés VIII',NULL),(71,'Internet de las Cosas',NULL),(72,'Liderazgo de Equipos de Alto Desempeño',NULL),(73,'Mercadotecnia',NULL),(74,'Mercadotecnia Digital',NULL),(75,'Metodología del Diseño',NULL),(76,'Modelado Manual',NULL),(77,'Modelado y Animación Digital',NULL),(78,'Optativa I',NULL),(79,'Optativa II',NULL),(80,'Optativa III',NULL),(81,'Postproducción Audiovisual',NULL),(82,'Probabilidad y Estadística',NULL),(83,'Proceso de Diseño',NULL),(84,'Producción Audiovisual',NULL),(85,'Programación Estructurada',NULL),(86,'Programación Orientada a Objetos',NULL),(87,'Programación de Redes',NULL),(88,'Programación para Inteligencia Artificial',NULL),(89,'Prototipos y Software 3D',NULL),(90,'Proyecto Integrador I',NULL),(91,'Proyecto Integrador II',NULL),(92,'Proyecto Integrador III',NULL),(93,'Representación Geométrica',NULL),(94,'Representación Visual',NULL),(95,'Rigging 3D',NULL),(96,'Seguridad Informática',NULL),(97,'Seguridad en Redes',NULL),(98,'Semiótica y Hermenéutica',NULL),(99,'Sistemas Operativos',NULL),(100,'Tecnologías Disruptivas',NULL),(101,'Tópicos de Calidad para el Diseño de Software',NULL),(102,'Ética Profesional',NULL),(103,'Ética y Legislación en Tecnologías de la Información',NULL);
/*!40000 ALTER TABLE `tb_materia` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-20 16:14:05
