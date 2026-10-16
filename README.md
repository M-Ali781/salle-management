# Système de Gestion et de Recherche Avancée de Salles

**Auteur :** Ali Mahraoui  
**Version :** 1.0-SNAPSHOT  
**Technologies :** Java 8 / 17, JPA 2.2 (Hibernate 5.4), Base H2, JUnit 4, Maven  

---

## 📌 Présentation du Projet

Ce projet implémente une solution robuste et découplée pour la **gestion et la recherche multi-critères de salles de réunion**, intégrant le contrôle dynamique des disponibilités temporelles et la pagination côté serveur via **JPA Criteria API** et **JPQL**.

Le code a été intégralement restructuré et personnalisé sous l'arborescence de packages `ma.ali.mahraoui.recherche` dans le respect des meilleures pratiques de conception logicielle (Clean Code, architecture en couches, programmation défensive).

---

## 🏗️ Architecture du Projet

Le projet adopte une architecture modulaire en couches :

```text
src/
├── main/
│   ├── java/ma/ali/mahraoui/recherche/
│   │   ├── model/
│   │   │   ├── Salle.java                 # Entité JPA (nom, capacité, réservations)
│   │   │   └── Reservation.java           # Entité JPA avec validation automatique (@PrePersist)
│   │   ├── repository/
│   │   │   ├── SalleRepository.java       # Interface d'accès aux données
│   │   │   └── SalleRepositoryImpl.java   # Implémentation Criteria API & JPQL sécurisée
│   │   ├── service/
│   │   │   ├── SalleService.java          # Interface du service métier
│   │   │   └── SalleServiceImpl.java      # Orchestration et validation métier
│   │   ├── util/
│   │   │   ├── PaginationResult.java      # Conteneur immuable pour résultats paginés
│   │   │   └── ValidationRecherche.java   # Validateur strict des paramètres & créneaux
│   │   └── ApplicationRecherche.java      # Point d'entrée exécutable (CLI & console H2)
│   └── resources/
│       └── META-INF/
│           └── persistence.xml            # Configuration JPA (Unité 'SallePU', H2 dialect)
└── test/
    └── java/ma/ali/mahraoui/recherche/
        └── RechercheSallesTest.java       # 10 tests unitaires & d'intégration complets
```

---

## 🚀 Fonctionnalités Clés

1. **Recherche de Salles Libres (JPQL) :**
   - Requête optimisée éliminant les salles ayant des réservations en chevauchement direct (`:debut < r.fin AND :fin > r.debut`).
   - Tolérance stricte des créneaux adjacents (ex: une réunion se terminant à 11h n'entre pas en conflit avec une réunion débutant à 11h).

2. **Recherche Multi-Critères Dynamique (Criteria API) :**
   - Filtrage combinable par nom (insensible à la casse, `LIKE %...%`), capacité minimale et créneau horaire.
   - Protection totale contre les caractères spéciaux SQL (`%`, `_`, `!`) via un mécanisme d'échappement rigoureux.

3. **Pagination Immuable Côté Serveur :**
   - Calcul fiable du nombre de pages, de l'index de départ (`offset`) et du total des éléments.
   - Prévention des débordements d'index et gestion des pages hors limites.

4. **Intégrité Relationnelle Bidirectionnelle :**
   - Synchronisation automatique entre `Salle` et `Reservation` avec cascade complète et collection immuable en lecture.

---

## 🛠️ Instructions d'Exécution

### 1. Compilation et validation des tests unitaires
```bash
mvn clean test
```
*Exécute 10 tests couvrant les chevauchements, les requêtes Criteria, la pagination et le cycle de vie JPA.*

### 2. Lancement de la démonstration console
```bash
mvn compile exec:java
```

### 3. Lancement avec accès à la console Web H2
```bash
mvn compile exec:java -Dexec.args="--console"
```
*La console H2 sera alors accessible sur `http://localhost:8082` (URL JDBC : `jdbc:h2:mem:recherche`).*
