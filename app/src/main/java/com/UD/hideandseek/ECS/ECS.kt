package com.UD.hideandseek.ECS

// 1. La interfaz base. Cualquier clase que sea un "Dato" del juego la implementará.
interface Component

class ECS {

    // 2. La Entidad: Un contenedor dinámico de componentes.
    class Entity {
        private val components = mutableMapOf<Class<out Component>, Component>()

        // --- 1. MÉTODOS BASE (Compatibles con Java y base de la lógica) ---

        fun <T : Component> add(c: T, cls: Class<T>): Entity {
            components[cls] = c
            return this
        }
        @Suppress("UNCHECKED_CAST")
        fun <T : Component> get(cls: Class<T>): T? = components[cls] as? T
        fun <T : Component> has(cls: Class<T>): Boolean = components.containsKey(cls)



        inline fun <reified T : Component> set(c: T): Entity = add(c, T::class.java)

        inline fun <reified T : Component> get(): T? = get(T::class.java)

        // Llama directamente al metodo has(cls) base
        inline fun <reified T : Component> has(): Boolean = has(T::class.java)
    }

    // 3. El Sistema: Modifica los componentes. No guarda estado de las entidades.
    interface System {
        // dt = Delta Time (el tiempo que pasó entre el fotograma anterior y el actual)
        fun update(entities: List<Entity>, dt: Float)
    }

    // 4. El Mundo: Une el juego.
    class World {
        val entities = mutableListOf<Entity>()
        private val systems = mutableListOf<System>()

        fun addSystem(s: System) = systems.add(s)
        fun addEntity(e: Entity): Entity { entities.add(e); return e }

        // Esta función se ejecuta unas 60 veces por segundo.
        // Le pasa la lista de TODOS los objetos del juego a TODOS los sistemas para que actúen.
        fun update(dt: Float) {
            systems.forEach { it.update(entities, dt) }
        }
    }
}
