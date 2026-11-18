<div align="center">
  <img src="https://placehold.co/1200x250/0d1117/58a6ff?text=NOTHINGAN+PRI+S.A.&font=montserrat" width="100%" alt="Nothingan Pri S.A." />
</div>

<br/>

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

<div align="center">
  <a href="https://github.com/banfruns-ban/Nothingan-Pri-S.A./blob/main/Memoria%20Nothingan%20Pri%20S.A.pdf" target="_blank">
    <img src="https://img.shields.io/badge/📄_Memòria_Tècnica_i_Manual_d'Usuari_(PDF)-D32F2F?style=for-the-badge&logo=adobeacrobatreader&logoColor=white" alt="Memòria Tècnica PDF" />
  </a>
</div>

<br/>

## 📋 Descripció del Projecte
**Nothingan Pri S.A.** és una plataforma web full-stack desenvolupada com a projecte intermodular de **2n de Desenvolupament d'Aplicacions Multiplataforma (DAM)** (Grup 3 — Curs 2026-27, Institut Nicolau Copèrnic, Terrassa). Digitalitza la gestió integral de lloguer de vehicles mitjançant un sistema **ERP i CRM** centralitzat.

## 🚀 Funcionalitats Principals
* **Portal Client:** Registre amb càrrega de DNI/carnet (`RF01`, `RF02`), catàleg filtrable per dates/horaris amb bloqueig automàtic de vehicles (`RF11`, `RF12`), descompte *Premium* i sistema de valoracions (1–5 estrelles).
* **Panell d'Agent:** Dashboard en temps real (`RF18`), gestió completa de flota (`RF40`–`RF47`), lliurament i retorn amb evidència fotogràfica (`RF13`, `RF19`–`RF25`) i càlcul automàtic de retencions de fiança.
* **Panell d'Administrador:** Validació documental de nous clients amb notificació per correu (`RF04`, `RF15`), gestió d'agents (`RF10`–`RF15`) i informes econòmics de reserves (`RF91`).

## 🏗️ Arquitectura i Stack Tecnològic
* **Backend:** Java 17+, Spring Boot, Spring Data JPA, Spring Security, JavaMailSender
* **Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap 5, JavaScript
* **Bases de Dades:** MySQL (dades relacionals del negoci) + MongoDB (documents, fotografies d'inspecció, notificacions i logs)
* **Testing & Gestió:** JUnit 5, Jira (Scrum), Figma, Git / GitLab / GitHub

## ⚙️ Instal·lació i Execució
1. Clona el repositori: `git clone https://github.com/banfruns-ban/Nothingan-Pri-S.A..git`
2. Configura les credencials de MySQL, MongoDB i SMTP a `src/main/resources/application.properties`.
3. Executa el projecte: `mvn spring-boot:run`
4. Obre el navegador a `https://localhost:8443`

## 📄 Documentació i Manual d'Usuari
* 📘 **[Llegir Memòria Tècnica i Manual d'Usuari (PDF)](https://github.com/banfruns-ban/Nothingan-Pri-S.A./blob/main/Memoria%20Nothingan%20Pri%20S.A.pdf)**

## 👥 Development Team (Group 3 — 2n DAM)
- **[Hugo López Soldado](https://github.com/hlopezsoldado)** — *Agent Management Module, Role-Based Security, Real-Time Dashboard & JUnit Testing*
- **[Izan Lázaro Egea](https://github.com/izan-tech)** — *Full-Stack & Backend Architecture (Spring Boot & MongoDB), Client & Booking Modules, System Validation & Bug Fixing*
- **[Brais Anfruns Rodríguez](https://github.com/banfruns-ban)** — *Fleet & Vehicle Management Module (Full CRUD & Availability Control), Functional QA & JUnit Unit/Integration Testing*
- **[Yuri Andrei Martinello](https://github.com/yuri-martinello)** — *Full-Stack Development, Authentication, Security & Access Permissions, Navigation UX Improvements & Booking Debugging*
