<!-- BANNER PRINCIPAL -->
<!-- BANNER PRINCIPAL -->
<div align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Montserrat&weight=800&size=36&duration=4000&pause=10000&color=58A6FF&background=0D1117&center=true&vCenter=true&width=850&height=110&lines=NOTHINGAN+PRI+S.A." alt="Nothingan Pri S.A." />
  <br/>
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=500&size=16&duration=3500&pause=1000&color=C9D1D9&background=0D1117&center=true&vCenter=true&width=850&height=45&lines=Plataforma+Integral+ERP+i+CRM+de+Lloguer+de+Vehicles;Java+%E2%80%A2+Spring+Boot+%E2%80%A2+MySQL+%E2%80%A2+MongoDB" alt="Subtítol" />
</div>

<div align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB" />
  <img src="https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5" />
</div>

<br/>

## 📋 Descripció del Projecte

**Nothingan Pri S.A.** és una plataforma web full-stack desenvolupada com a projecte intermodular de **2n de Desenvolupament d'Aplicacions Multiplataforma (DAM)** (Grup 3 — Curs 2026-27, Institut Nicolau Copèrnic, Terrassa).

L'empresa compta amb més de 36 anys d'experiència en el sector del lloguer de vehicles a Terrassa. Aquest sistema substitueix la gestió tradicional basada en paper i trucades telefòniques per una solució centralitzada **ERP i CRM** que digitalitza tot el cicle de vida del lloguer: registre i validació documental de clients, catàleg interactiu amb bloqueig automàtic de vehicles, gestió de flota, lliurament i retorn amb evidència fotogràfica, càlcul automàtic de fiances/penalitzacions i generació d'informes.

---

## 🚀 Funcionalitats Principals

### 👤 Portal Client
* **Registre i Verificació Documental (`RF01`, `RF02`, `RF81`):** Formulari d'alta amb càrrega d'imatges del DNI/Passaport i carnet de conduir per a la seva revisió.
* **Catàleg Dinàmic i Filtratge (`RF11`, `RF16`, `RF17`):** Consulta de vehicles disponibles filtrats per dates, horaris i categoria, amb càlcul automàtic del preu total i descompte del 25% en la fiança per a clients *Premium*.
* **Gestió de Reserves (`RF05`, `RF12`, `RF26`, `RF28`):** Bloqueig automàtic del vehicle en confirmar la reserva, consulta d'històric i anul·lació prèvia al lliurament.
* **Sistema de Valoracions:** Ressenyes de 1 a 5 estrelles i comentaris verificats un cop finalitzat el lloguer.

### 🧑‍💼 Panell d'Agent
* **Dashboard en Temps Real (`RF18`):** Resum operatiu diari amb reserves pendents de lliurament, retorns en curs, vehicles actius i llistat de propers lliuraments.
* **Gestió Integral de Flota (`RF08`, `RF09`, `RF40`–`RF47`):** Alta, baixa, modificació, activació/desactivació i filtratge multi-criteri de vehicles (matrícula, marca, model, any i disponibilitat).
* **Lliurament de Vehicles (`RF13`, `RF19`):** Checklist d'estat inicial, lectura d'odòmetre/combustible i captura fotogràfica dels 4 costats més fotografies addicionals il·limitades.
* **Retorn i Liquidació de Fiança (`RF20`–`RF25`):** Inspecció en devolució, registre de desperfectes imputables o no imputables, adjunció de fotos d'incidències, càlcul automàtic de retencions per danys, carburant o retard i tancament del contracte.
* **Gestió de Clients (`RF06`, `RF07`, `RF23`–`RF26`):** Alta directa des d'oficina, cerca per DNI/email, filtratge avançat i assignació de categoria *Premium*.

### 🛡️ Panell d'Administrador
* **Validació de Nous Clients (`RF03`, `RF04`, `RF14`, `RF15`, `RF82`):** Revisió de documentació adjunta, aprovació o rebuig de sol·licituds i notificació automàtica per correu electrònic.
* **Gestió d'Agents (`RF10`–`RF15`):** Control complet d'empleats i permisos d'accés basats en rols (`CLIENT`, `AGENT`, `ADMIN`).
* **Informes i Històric (`RF36`, `RF37`, `RF91`):** Consulta d'informes econòmics i estadístics de reserves filtrats per client, vehicle i dates.

---

## 🏗️ Arquitectura i Stack Tecnològic

El projecte segueix una **arquitectura MVC per capes** (`Entity`, `Repository`, `Service`, `Controller` i vistes `Thymeleaf`) combinant persistència híbrida:

| Capa / Àmbit | Tecnologies |
| :--- | :--- |
| **Backend** | Java 17+, Spring Boot, Spring Data JPA, Spring Security, JavaMailSender |
| **Frontend** | Thymeleaf, HTML5, CSS3, Bootstrap 5, JavaScript |
| **Base de Dades Relacional** | **MySQL** (Usuaris, Clients, Agents, Administradors, Vehicles, Reserves, Incidències i Ressenyes) |
| **Base de Dades NoSQL** | **MongoDB** (Documents d'identitat, fotografies d'inspecció, notificacions i logs del sistema) |
| **Testing & QA** | JUnit 5 (Tests unitaris i d'integració de serveis) |
| **Gestió & Disseny** | Jira (4 Sprints Scrum), Figma (Prototipatge UI/UX), Git / GitLab / GitHub |

---

## ⚙️ Instal·lació i Posada en Marxa

### Prerequisits
* **JDK 17** o superior
* **Maven 3.8+**
* **MySQL Server** (port `3306`)
* **MongoDB** (port `27017`)

### Passos d'execució

1. **Clonar el repositori:**
   ```bash
   git clone [https://github.com/banfruns-ban/Nothingan-Pri-S.A..git](https://github.com/banfruns-ban/Nothingan-Pri-S.A..git)
   cd Nothingan-Pri-S.A.
