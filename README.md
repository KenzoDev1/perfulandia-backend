# 🧾 Proyecto: Transformación Digital - Perfulandia SPA


Este repositorio contiene el desarrollo técnico del sistema basado en microservicios para la empresa Perfulandia SPA, como parte de la Evaluación Parcial 2 de la asignatura **Desarrollo Full Stack I**.

## 📦 Descripción General del Proyecto

> 📝 Explicar brevemente en qué consiste el sistema, qué problema resuelve y qué beneficios ofrece al reemplazar el antiguo sistema monolítico.

## 🧩 Arquitectura de Microservicios

> 📝 Describir cómo está estructurado el sistema en microservicios. Pueden incluir un diagrama y explicar brevemente la función de cada servicio.

### Microservicios Desarrollados

- `productservice`: > 📝 Microservicio dedicado a Insertar, Eliminar, Buscar y Listar productos.
- `usuarioservice`: > 📝 Microservicio dedicado a Listar, Buscar, Insertar y Eliminar usuarios.
- `carritoservice`: > 📝 .

## 🛠️ Tecnologías Utilizadas

- `IntelliJ`
- `GitHub`
- `MySQL (Laragon)`
- `Lombok`
- `Maven`
- `Spring web`
- `Spring Data JPA`
- `Spring Boot Dev Tools`
- `MySQL Driver`
- `Postman`

## 🗄️ Configuración de Bases de Datos

> 📝 Indicar qué motor de base de datos usaron, cómo configuraron la conexión (`application.properties`), y qué tablas y campos definieron para cada microservicio.

## 📮 Endpoints y Pruebas

> 📝 Especificar los principales endpoints disponibles por microservicio (CRUD y llamadas entre servicios).  
> Incluir capturas o descripciones de pruebas realizadas con Postman (mínimo 3 por micro-servicio).

## 🧑‍💻 Integrantes del Equipo

> 📝 Indicar nombre completo y rol de cada integrante del equipo.

| Nombre                  | Rol en el proyecto         |
|-------------------------|----------------------------|
| Vicente Alarcón Gallardo| Documentación .README      |
| Ignacio Bittner Navea   | Desarrollo de la API       |
| Benjamin Martinez Oyarzo| Desarrollo de la API       |
| Francisco Aránguiz Inostroza| Desarrollo Informe     |

## 📂 Estructura del Repositorio

> 📝 Explicar brevemente la organización de carpetas del repositorio (por ejemplo, cada carpeta corresponde a un microservicio separado con su propio `pom.xml`).

```

📦 perfulandia-microservices
├── usuarioservice
├── productoservice
├── pedidoservice (Ejemplo)
├── notificacionservice (Ejemplo)
└── README.md

```

## 👥 Colaboración en GitHub

> Cada Integrante cuenta con su propia rama en la cual trabaja sus propios avances, y mejorando codigo u otro documentos de los demás.
![Captura de pantalla](readme-assets/branch%20example.png "Captura de pantalla de las branch existentes del equipo.")
Descripcion de las Ramas:  
    - `Master`: Esta es la rama principal, en la cual se trabaja cuando queremos hacer un cambio rapido al repositorio.  
    <br>
    - `Rama-Vicente`: Como dice el nombre, esta rama pertenece al integrante `Vicente Alarcón`, el cual trabaja exclusivamente en esta rama.  
    <br>
    - `Rama-Nacho`: Al igual que la anterior, Esta rama pertenece al integrante `Ignacio Bittner`. Rama la cual esta destinada al uso exclusivo del integrante el cual lleva su nombre en la rama.  
    <br>
    - `Rama-Francisco`: Rama perteneciente al integrante `Francisco Aranguiz`, rama destinada al mismo uso que las anteriores.  
    <br>
    - `Rama-Benjamin`: Rama perteneciente al integrante `Benjamin Martinez`, rama destinada para el uso exclusivo del integrante el cual lleva su nombre esta rama.  
    <br>
    Si bien ya se menciono, cabe aclarar que cada rama es de uso EXCLUSIVO para el integrante cuyo nombre aparezca en la rama, por ejemplo:  
    `Rama-Vicente` ---> SOLO PODRA SER USADA POR `VICENTE ALARCÓN`  
    <br>
    Esto se hizo de esta forma, para darle a cada integrante su propio espacio en el cual puedan desenvolverse a gusto, creando y testeando su codigo para finalmente enviar un `pull request` hacia la rama principal `Master`.  
    


📝 Explicar cómo se organizó el trabajo en ramas (`master`, `pruebas`), frecuencia de commits y cómo se coordinaron como equipo.

## 📈 Lecciones Aprendidas

> 📝 Reflexionar brevemente sobre qué aprendieron durante el desarrollo del proyecto (técnico y en trabajo en equipo).

---

[Guía Oficial en Notion – Evaluación Parcial 2 (35%)](https://quilt-canary-969.notion.site/Gu-a-Oficial-Evaluaci-n-Parcial-2-35-1f75b3c4e31280aaab79c9a71f1cfb7b?pvs=4)