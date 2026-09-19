# Documentación Detallada: Snake Game 🐍

Este documento explica cómo funciona el código del juego paso a paso. Está pensado para que puedas leerlo y entender la lógica matemática y de programación que hace que la Serpiente cobre vida en tu pantalla.

---

## 1. El Flujo de la App (Las Actividades)

La aplicación tiene 4 pantallas (Activities). Las Actividades normales (`MainActivity`, `OptionsActivity`, `SkinActivity`) funcionan de la manera clásica de Android: tienen un archivo visual (`.xml`) y un archivo lógico (`.java`).

1. **MainActivity:** Es el menú. Usa `findViewById()` para buscar los botones y `setOnClickListener()` para escuchar los clics. Usa `Intent` para viajar a las otras pantallas.
2. **OptionsActivity / SkinActivity:** Usan `SharedPreferences`. Esto es como una pequeña base de datos del teléfono (un "cajón" de memoria). 
   * Para leer: `prefs.getInt("nombre_del_dato", valor_por_defecto)`
   * Para guardar: `editor.putInt("nombre", valor_nuevo); editor.apply();`
3. **GameActivity:** Esta actividad es especial. No carga un archivo `.xml`. En su lugar, hace `setContentView(gameView);`. Es decir, la pantalla entera es el "lienzo" (GameView) que dibujaremos a mano. Su única tarea es avisarle al `GameView` cuándo el juego se pausa (minimizar la app) y cuándo se reanuda, usando los métodos `onPause()` y `onResume()`.

---

## 2. El Motor del Juego: `GameView.java` (Línea por Línea)

Este es el archivo más complejo e importante. No es una pantalla normal, es un **SurfaceView**. Un `SurfaceView` es un lienzo especial de Android diseñado para videojuegos porque permite dibujar gráficos muy rápido usando un "Hilo" (`Thread`) secundario para no trabar el teléfono.

### A. Las Variables Principales
* `private Thread gameThread;`: El hilo secundario que corre el juego en paralelo a Android.
* `private volatile boolean running = false;`: Un interruptor para prender o apagar el juego. `volatile` significa que este valor puede ser modificado por otros hilos de forma segura.
* `private final SurfaceHolder holder;`: El "Holder" es el que sostiene el lienzo. Es el guardián que nos da permiso para dibujar en la pantalla.
* `private long moveDelayMillis;`: Los milisegundos que espera la serpiente para dar el próximo paso. (Menor número = Más velocidad).
* `private final LinkedList<Point> snake;`: La serpiente. Una `LinkedList` (Lista Enlazada) es perfecta aquí. Un `Point` guarda una coordenada `(x, y)`. La cabeza de la serpiente es el primer elemento de la lista, y la punta de la cola es el último.

### B. El Constructor `GameView(Context context)`
Cuando arranca la pantalla, este método se ejecuta una vez.
1. `holder = getHolder(); holder.addCallback(this);`: Le decimos al guardián del lienzo que nosotros (esta clase) nos vamos a encargar de dibujar.
2. Aquí se abre `SharedPreferences` para leer la velocidad y el color (`snakeColor`) que el usuario guardó en Opciones/Skins.

### C. El "Game Loop" (Método `run()`)
Todo videojuego del mundo funciona con un "Bucle infinito" que hace tres cosas repetidamente: Chequear tiempo, Actualizar lógica, y Dibujar.
```java
while (running) {
    long now = System.currentTimeMillis(); // Qué hora es exactamente ahora en milisegundos.
    
    // Si ya pasó el tiempo necesario (moveDelayMillis) desde el último paso...
    if (!gameOver && now - lastMoveTime >= moveDelayMillis) {
        update(); // Movemos a la serpiente y revisamos colisiones.
        lastMoveTime = now; // Reiniciamos el reloj.
    }
    
    draw(); // Dibujamos todo (fondo, comida, serpiente) en sus nuevas posiciones.
    
    // Hacemos que el procesador "descanse" 10 milisegundos para no quemar la batería del teléfono intentando dibujar a miles de FPS.
    Thread.sleep(10);
}
```

### D. La Lógica del Juego (Método `update()`)
Aquí ocurre la magia de la serpiente.
1. **Calcular la nueva cabeza:** 
   * Se agarra la posición de la cabeza actual: `Point head = snake.getFirst();`
   * Dependiendo de hacia dónde miramos (`currentDirection`), creamos un `newHead` (nueva Cabeza). Si vamos a la derecha, le sumamos 1 a la `x`. Si vamos abajo, sumamos 1 a la `y`.
2. **Colisiones (Perder):**
   * ¿Chocó la pared? `if (newHead.x < 0 || newHead.x >= numColumns ... )`
   * ¿Chocó consigo misma? `if (snake.contains(newHead))` (Si el cuerpo ya ocupa la casilla donde quiere ir la nueva cabeza).
   * Si pasa alguna, `gameOver = true; return;` (termina el update).
3. **El Movimiento (La genialidad de la Lista Enlazada):**
   * Siempre agregamos la nueva cabeza al principio: `snake.addFirst(newHead);`
   * **¿Comió?** `boolean ateFood = newHead.equals(food);`
   * Si comió, sumamos un punto y creamos comida nueva. Como **NO** borramos la cola, la serpiente tiene un bloque más de largo. (¡Creció!).
   * Si NO comió, tenemos que borrar el último pedazo de la cola: `snake.removeLast();`. Al agregar un bloque adelante y borrar uno atrás, da la ilusión perfecta de que la serpiente "avanzó".

### E. Dibujando en la Pantalla (Método `draw()`)
El `update()` solo cambió números en la memoria. `draw()` los hace realidad.
1. `Canvas canvas = holder.lockCanvas();`: Le pedimos al guardián que "congele" el lienzo y nos lo preste para pintar.
2. `canvas.drawColor(Color.BLACK);`: Pintamos todo de negro para borrar el cuadro (frame) anterior. Si no hacemos esto, la serpiente dejaría una estela por donde pasa.
3. **Comida:** Pintamos un cuadradito rojo multiplicando sus coordenadas por el `cellSize` (el tamaño de cada cuadro de la grilla en píxeles de pantalla).
4. **Serpiente:** 
   ```java
   paint.setColor(snakeColor); // Usamos el color que leíste de las Opciones
   for (Point p : snake) { // Por cada coordenada que conforma a la serpiente...
       canvas.drawRect(...); // Dibujar un rectángulo verde/azul/etc en esa posición
   }
   ```
5. `holder.unlockCanvasAndPost(canvas);`: Le devolvemos el lienzo al guardián para que lo muestre en la pantalla del celular.

### F. Controles (Método `onTouchEvent()`)
Aquí captamos cuando el usuario arrastra el dedo por la pantalla.
1. `ACTION_DOWN`: El dedo toca la pantalla. Guardamos esa coordenada inicial (`touchStartX`, `touchStartY`).
2. `ACTION_UP`: El dedo se levanta. Comparamos dónde se levantó contra dónde se apoyó.
   * `deltaX` = La distancia horizontal que recorrió el dedo.
   * `deltaY` = La distancia vertical.
3. Si moviste el dedo más en horizontal que en vertical `Math.abs(deltaX) > Math.abs(deltaY)`, es un "Swipe Horizontal".
   * Si `deltaX` es positivo (terminó más a la derecha que donde empezó), miramos a la `RIGHT`.
   * Para evitar que la serpiente se de la vuelta instantáneamente y se suicide, verificamos: `if (currentDirection != Direction.LEFT) { nextDirection = Direction.RIGHT }`.

---
*Este documento te servirá como referencia por si querés agregarle mecánicas (como bombas, paredes en el medio, o multiplicadores de puntaje).*