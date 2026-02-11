# Juego Sostenibilidad

## Memoria Técnica del Proyecto

**Autor:** Pau Silvestre Arnandis  
**Asignatura:** Programación Multimedia  
**Ciclo:** 2º DAW  
**Plataforma:** Android  
**Versión:** 1.0  

---

## Índice

1. [Descripción del Proyecto](#descripción-del-proyecto)
2. [Relación con la Sostenibilidad](#relación-con-la-sostenibilidad)
3. [Arquitectura y Estructura del Código](#arquitectura-y-estructura-del-código)
4. [Funcionalidades Principales](#funcionalidades-principales)
5. [Elementos Multimedia](#elementos-multimedia)
6. [Decisiones Técnicas](#decisiones-técnicas)
7. [Requisitos del Sistema](#requisitos-del-sistema)
8. [Instrucciones de Compilación](#instrucciones-de-compilación)
9. [Referencias y Recursos](#referencias-y-recursos)

---

## Descripción del Proyecto

Juego Sostenibilidad es un juego para dispositivos Android de tipo idle/clicker con temática medioambiental. El jugador debe recoger basura de diferentes zonas marinas y costeras para limpiar el medio ambiente, gestionando recursos económicos y contratando limpiadores automáticos.

El objetivo principal es concienciar sobre la problemática de la contaminación marina mientras se ofrece una experiencia de juego entretenida con mecánicas de progresión.

---

## Relación con la Sostenibilidad

La sostenibilidad es el eje central del proyecto y está integrada en múltiples niveles:

### Mecánica de Juego
- El jugador recoge residuos de diferentes tipos: plástico, orgánico, metal y vidrio
- Cada tipo de basura tiene un peso y valor asociado, reflejando su impacto medioambiental
- Las zonas representan ecosistemas marinos reales: playa, océano, arrecife de coral y mar profundo
- El sistema de progresión incentiva la limpieza continua del entorno

### Contenido Educativo
El juego muestra datos reales sobre contaminación marina durante la partida:

- "Una botella de plástico tarda 450 años en degradarse"
- "El 80% de la basura marina proviene de tierra firme"
- "8 millones de toneladas de plástico llegan al océano cada año"
- "Las tortugas confunden bolsas de plástico con medusas"
- "En 2050 habrá más plástico que peces en el océano"

### Personajes con Propósito
Los limpiadores representan diferentes niveles de compromiso con el medio ambiente:
- **Voluntario:** Representa el activismo ciudadano
- **Trabajador:** Profesionales de la limpieza medioambiental
- **Robot:** Tecnología al servicio de la sostenibilidad

---

## Arquitectura y Estructura del Código

El proyecto sigue una arquitectura modular con separación clara de responsabilidades:

```
app/src/main/java/com/example/juegosostenibilidad/
├── MainActivity.java          # Activity principal
├── GameView.java              # Vista y game loop (SurfaceView)
├── GameManager.java           # Lógica central del juego
├── UIManager.java             # Interfaz de usuario
├── Trash.java                 # Entidad de basura
├── Cleaner.java               # Entidad de limpiador
├── ObjectPool.java            # Pool de objetos reutilizables
├── IconManager.java           # Gestión de iconos vectoriales
├── KenneyAssetManager.java    # Assets gráficos de Kenney
├── SoundManager.java          # Gestión de audio
└── GameConstants.java         # Constantes del juego
```

### Patrones de Diseño Utilizados

- **Singleton:** Utilizado en IconManager y KenneyAssetManager para gestionar recursos compartidos
- **Object Pool:** Implementado en ObjectPool.java para reutilizar objetos Trash y reducir la carga del Garbage Collector
- **Observer:** Interfaz GameEventListener para comunicar eventos del juego

---

## Funcionalidades Principales

### Sistema de Juego
- Recogida manual de basura mediante interacción táctil
- Spawn automático de basura con probabilidad configurable
- Sistema de dinero para comprar mejoras
- Cuatro zonas desbloqueables con diferentes multiplicadores de valor

### Sistema de Limpiadores (IA)
- Tres tipos de limpiadores con diferentes velocidades y capacidades
- Movimiento automático hacia la basura más cercana
- Sistema de carga y descarga de residuos
- Animaciones diferenciadas por tipo

### Sistema de Progresión
- Desbloqueo de nuevas zonas mediante dinero
- Sistema de Rebirth que reinicia el progreso con bonificaciones permanentes
- Multiplicador de valor y velocidad de spawn por cada rebirth
- Guardado automático del progreso con SharedPreferences

### Interfaz de Usuario
- HUD con información de dinero, zona actual y estadísticas
- Panel de tienda para comprar limpiadores y zonas
- Control de velocidad de juego (x1, x2, x4)
- Mensajes educativos mostrados durante el gameplay

---

## Elementos Multimedia

### Gráficos

#### Renderizado Personalizado
- Uso de Canvas para dibujar todos los elementos del juego
- Gradientes y shaders para efectos visuales mejorados
- Animaciones procedurales para basura y limpiadores
- Diseño de personajes mediante código con formas geométricas

#### Iconos Vectoriales
Se han creado 28 iconos vectoriales (VectorDrawable) para la interfaz:
- Iconos de tipos de basura (plástico, orgánico, metal, vidrio)
- Iconos de zonas (playa, océano, arrecife, mar profundo)
- Iconos de UI (moneda, tienda, ubicación, candado, etc.)

#### Assets de Kenney
Integración de assets gráficos de la librería Kenney para elementos de interfaz adicionales.

### Audio
- Sistema de sonidos gestionado por SoundManager
- Efectos de sonido para acciones del jugador

### Optimizaciones Gráficas
- Game loop a 60 FPS con control de deltaTime
- Doble buffer mediante SurfaceView para renderizado suave
- Caché de Bitmaps para iconos frecuentes
- Object Pool para evitar instanciación constante

---

## Decisiones Técnicas

### Elección de SurfaceView
Se optó por SurfaceView en lugar de View tradicional para:
- Renderizado en thread separado sin bloquear el UI thread
- Control preciso del frame rate
- Mayor rendimiento en animaciones continuas

### Object Pool Pattern
Implementación de pool de objetos para la clase Trash:
- Reduce la presión sobre el Garbage Collector
- Evita stuttering durante el gameplay
- Configuración de tamaño inicial (15) y máximo (25) objetos

### Persistencia de Datos
Uso de SharedPreferences para guardar:
- Dinero acumulado
- Estadísticas (kg y items recogidos)
- Zonas desbloqueadas
- Cantidad de limpiadores por tipo
- Contador de rebirths

### Configuración Centralizada
Todas las constantes del juego están en GameConstants.java:
- Facilita el balanceo del juego
- Permite ajustes rápidos sin modificar múltiples archivos
- Mejora la mantenibilidad del código

### Thread Safety
Uso de CopyOnWriteArrayList para colecciones accedidas desde múltiples threads:
- Lista de limpiadores
- Lista de objetos activos en el pool

---

## Requisitos del Sistema

| Requisito | Valor |
|-----------|-------|
| Android mínimo | API 26 (Android 8.0 Oreo) |
| Android objetivo | API 34 (Android 14) |
| Orientación | Portrait (vertical) |
| Permisos | Ninguno requerido |

### Dependencias
- androidx.appcompat
- com.google.android.material
- JUnit (testing)
- Espresso (testing UI)

---

## Referencias y Recursos

### Librerías y Dependencias

| Librería | Versión | Descripción | Enlace |
|----------|---------|-------------|--------|
| AndroidX AppCompat | 1.6.1 | Compatibilidad con versiones anteriores de Android | [Maven Repository](https://mvnrepository.com/artifact/androidx.appcompat/appcompat) |
| Material Components | 1.11.0 | Componentes de Material Design para Android | [Material Design](https://material.io/develop/android) |
| JUnit | 4.13.2 | Framework de testing para Java | [JUnit](https://junit.org/junit4/) |
| Espresso | 3.5.1 | Framework de testing de UI para Android | [Android Testing](https://developer.android.com/training/testing/espresso) |

### Assets Gráficos

| Recurso | Autor | Licencia | Enlace |
|---------|-------|----------|--------|
| Kenney Game Assets | Kenney.nl | CC0 (Dominio Público) | [Kenney Assets](https://kenney.nl/assets) |
| Kenney Fonts | Kenney.nl | CC0 (Dominio Público) | [Kenney Fonts](https://kenney.nl/assets/kenney-fonts) |

### Documentación Oficial Consultada

- **Android Developer Documentation** - Guías oficiales de desarrollo Android  
  [https://developer.android.com/docs](https://developer.android.com/docs)

- **SurfaceView y Game Loop** - Implementación de bucle de juego  
  [https://developer.android.com/reference/android/view/SurfaceView](https://developer.android.com/reference/android/view/SurfaceView)

- **Canvas y Gráficos 2D** - Renderizado personalizado con Canvas  
  [https://developer.android.com/reference/android/graphics/Canvas](https://developer.android.com/reference/android/graphics/Canvas)

- **SharedPreferences** - Persistencia de datos ligera  
  [https://developer.android.com/training/data-storage/shared-preferences](https://developer.android.com/training/data-storage/shared-preferences)

- **VectorDrawable** - Gráficos vectoriales escalables  
  [https://developer.android.com/develop/ui/views/graphics/vector-drawable-resources](https://developer.android.com/develop/ui/views/graphics/vector-drawable-resources)

### Guías de Diseño

- **Material Design Guidelines** - Principios de diseño de interfaces  
  [https://m3.material.io/](https://m3.material.io/)

### Datos Medioambientales

Los datos educativos mostrados en el juego provienen de fuentes oficiales:

- **Ocean Conservancy** - Estadísticas sobre contaminación marina  
  [https://oceanconservancy.org/](https://oceanconservancy.org/)

- **National Geographic** - Datos sobre plásticos en el océano  
  [https://www.nationalgeographic.com/environment/](https://www.nationalgeographic.com/environment/)

- **UNEP (Programa de las Naciones Unidas para el Medio Ambiente)**  
  [https://www.unep.org/](https://www.unep.org/)

---

## Conclusiones

El proyecto cumple con los objetivos de crear una aplicación Android funcional que integra la temática de sostenibilidad de forma coherente con las mecánicas de juego. Se han aplicado buenas prácticas de programación, patrones de diseño y optimizaciones de rendimiento para garantizar una experiencia de usuario fluida.

La estructura modular del código facilita futuras ampliaciones como nuevas zonas, tipos de basura o mecánicas adicionales.

---

**Autor:** Pau Silvestre Arnandis  
**Fecha:** Febrero 2026

