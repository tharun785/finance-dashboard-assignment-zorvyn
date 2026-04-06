# 💰 Finance Dashboard Application

A comprehensive full-stack finance management system with role-based access control (RBAC), JWT authentication, and real-time analytics dashboard.

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation Guide](#installation-guide)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

The Finance Dashboard Application is a secure, scalable web application that helps users manage their financial transactions, track income and expenses, and gain insights through interactive dashboards. The system implements role-based access control with three distinct user roles: Admin, Analyst, and Viewer.

### Key Highlights
- 🔐 **Secure Authentication**: JWT-based authentication with Spring Security
- 👥 **Role-Based Access**: Granular permissions for Admin, Analyst, and Viewer roles
- 📊 **Real-time Dashboard**: Interactive charts and analytics for financial data
- 💱 **Transaction Management**: Complete CRUD operations for financial records
- 📈 **Insights & Analytics**: Category-wise expense breakdown and income tracking
- 🎨 **Responsive UI**: Modern, responsive React frontend

## ✨ Features

### Authentication & Authorization
- User registration and login with JWT tokens
- Role-based access control (Admin, Analyst, Viewer)
- Token-based API security
- Password encryption using BCrypt

### Dashboard
- Real-time financial summary (Total Income, Expenses, Net Balance)
- Category-wise expense breakdown with visual charts
- Recent transaction history
- Quick insights and statistics

### Financial Records Management
- Create, read, update, and delete financial records
- Filter records by type, category, and date range
- Track income and expenses with categories
- View transaction history

### User Management (Admin only)
- View all registered users
- Create new users with specific roles
- Activate/deactivate user accounts
- Update user roles
- Delete user accounts

## 🛠 Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core programming language |
| Spring Boot | 3.1.5 | Application framework |
| Spring Security | 3.1.5 | Authentication & Authorization |
| Spring Data JPA | 3.1.5 | Database ORM |
| PostgreSQL | 15+ | Relational database |
| JWT | 0.11.5 | Token-based authentication |
| Maven | 3.8+ | Dependency management |
| Hibernate | 6.2+ | ORM implementation |
| Lombok | 1.18.30 | Boilerplate code reduction |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.2.0 | UI framework |
| React Router DOM | 6.20.1 | Routing |
| Axios | 1.6.2 | HTTP client |
| CSS3 | - | Styling |
| Recharts | 2.10.3 | Charts and graphs |

### Development Tools
- IntelliJ IDEA / VS Code
- Postman for API testing
- Git for version control
- Maven for build management
- npm for frontend dependencies

## 🏗 Architecture

### System Architecture


