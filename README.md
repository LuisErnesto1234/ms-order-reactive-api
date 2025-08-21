# 🚀 Sistema de Procesamiento de Órdenes Reactivo

Un sistema robusto de procesamiento de órdenes de compra construido con **Spring WebFlux** que implementa validaciones paralelas, reintentos automáticos y notificaciones multi-canal utilizando programación reactiva.

## 🏗️ Arquitectura del Sistema

El sistema procesa un flujo continuo de órdenes (`Flux<Order>`) implementando las siguientes características:

- **Validaciones Paralelas**: Verificación simultánea en 3 servicios externos (inventario, pagos, envío)
- **Procesamiento Reactivo**: Solo procesa órdenes que pasen TODAS las validaciones
- **Reintentos Inteligentes**: 2 intentos automáticos antes de descartar órdenes fallidas
- **Notificaciones Multi-canal**: Email y SMS a cada cliente al completar el procesamiento

## 🛠️ Stack Tecnológico

| Categoría | Tecnología | Propósito |
|-----------|------------|-----------|
| **Framework** | Spring Boot + WebFlux | Programación reactiva y no-bloqueante |
| **Base de Datos** | PostgreSQL + R2DBC | Acceso reactivo a datos |
| **Persistencia** | Hibernate R2DBC | ORM reactivo |
| **Build Tool** | Gradle | Gestión de dependencias y construcción |
| **Calidad de Código** | Checkstyle + Jacoco + SonarQube | Análisis estático, cobertura y calidad |
| **CI/CD** | Jenkins | Integración y despliegue continuo |
| **Seguridad** | JWT | Autenticación y autorización |
| **Validación** | Spring Validation | Validación de datos de entrada |
| **Mapeo de Objetos** | MapStruct | Transformación eficiente entre DTOs |
| **Productividad** | Lombok | Reducción de código boilerplate |
| **Notificaciones** | Spring Mail | Envío de correos electrónicos |

## 🔧 Requisitos Previos

- **Java 17** o superior
- **PostgreSQL 13+**
- **Gradle 8.0+**
- **Docker** (opcional, para contenedores)

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/reactive-order-processing.git
cd reactive-order-processing
```

### 2. Configurar Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE reactive_orders;

-- Configurar usuario (opcional)
CREATE USER order_processor WITH PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE reactive_orders TO order_processor;
```

### 3. Configurar Variables de Entorno
```bash
# application.yml o variables de entorno
DATABASE_URL=r2dbc:postgresql://localhost:5432/reactive_orders
DATABASE_USERNAME=order_processor
DATABASE_PASSWORD=tu_password
JWT_SECRET=tu_jwt_secret_key
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password
SMS_API_KEY=tu_sms_api_key
```

### 4. Construir y Ejecutar
```bash
# Construir el proyecto
./gradlew build

# Ejecutar tests con cobertura
./gradlew test jacocoTestReport

# Ejecutar aplicación
./gradlew bootRun

# O usando JAR
java -jar build/libs/reactive-order-processing-1.0.0.jar
```

## 📋 Análisis de Calidad

### Ejecutar Checkstyle
```bash
./gradlew checkstyleMain checkstyleTest
```

### Generar Reporte de Cobertura (Jacoco)
```bash
./gradlew jacocoTestReport
# Reporte disponible en: build/reports/jacoco/test/html/index.html
```

### Análisis con SonarQube
```bash
# Iniciar SonarQube local
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Ejecutar análisis
./gradlew sonarqube \
  -Dsonar.projectKey=reactive-order-processing \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=tu_token
```

## 🔄 Pipeline CI/CD con Jenkins

El proyecto incluye un `Jenkinsfile` que implementa:

1. **Build**: Compilación con Gradle
2. **Test**: Ejecución de tests unitarios e integración
3. **Quality Gate**: Verificación con Checkstyle, Jacoco y SonarQube
4. **Package**: Creación de JAR ejecutable
5. **Deploy**: Despliegue automático (configurar según entorno)

### Configurar Jenkins Pipeline
```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
        stage('Test & Quality') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh './gradlew test jacocoTestReport'
                    }
                }
                stage('Code Quality') {
                    steps {
                        sh './gradlew checkstyleMain sonarqube'
                    }
                }
            }
        }
    }
}
```

## 🏃‍♂️ Uso de la API

### Endpoint Principal
```http
POST /api/v1/orders/process
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "customerId": "12345",
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2,
      "price": 29.99
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "Lima",
    "country": "PE"
  }
}
```

### Respuesta
```json
{
  "orderId": "ORDER-789",
  "status": "PROCESSING",
  "validations": {
    "inventory": "PENDING",
    "payment": "PENDING", 
    "shipping": "PENDING"
  },
  "estimatedCompletion": "2025-08-21T15:30:00Z"
}
```

## 🧪 Testing

### Estructura de Tests
```
src/test/java/
├── unit/                 # Tests unitarios
├── integration/          # Tests de integración
└── e2e/                 # Tests end-to-end
```

### Ejecutar Tests
```bash
# Todos los tests
./gradlew test

# Solo tests unitarios
./gradlew test --tests "*unit*"

# Solo tests de integración
./gradlew test --tests "*integration*"

# Con perfil específico
./gradlew test -Dspring.profiles.active=test
```

## 📊 Monitoreo y Métricas

El sistema expone métricas a través de **Spring Boot Actuator**:

- `/actuator/health` - Estado de la aplicación
- `/actuator/metrics` - Métricas de rendimiento
- `/actuator/prometheus` - Métricas para Prometheus
- `/actuator/loggers` - Configuración de logging

## 🔧 Configuración Avanzada

### Configuración de Reintentos
```yaml
order:
  processing:
    retry:
      maxAttempts: 3
      backoff:
        delay: 1000ms
        multiplier: 2.0
        maxDelay: 10000ms
```

### Configuración de Validadores Externos
```yaml
external:
  services:
    inventory:
      url: ${INVENTORY_SERVICE_URL:http://localhost:8081}
      timeout: 5s
    payment:
      url: ${PAYMENT_SERVICE_URL:http://localhost:8082}
      timeout: 10s
    shipping:
      url: ${SHIPPING_SERVICE_URL:http://localhost:8083}
      timeout: 3s
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Hacer commit de tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear un Pull Request

### Estándares de Código
- Seguir las reglas de **Checkstyle** configuradas
- Mantener cobertura de tests > 80%
- Pasar el **Quality Gate** de SonarQube
- Documentar métodos públicos con JavaDoc

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

## 📞 Soporte

- **Issues**: [GitHub Issues](https://github.com/LuisErnesto1234/ms-order-reactive-api/issues)
- **Documentación**: [Wiki del Proyecto](https://github.com/LuisErnesto1234/ms-order-reactive-api/wiki)
- **Email**: luisernestodazafirma@gmail.com

---

⭐ **¡Si este proyecto te resulta útil, considera darle una estrella!** ⭐
