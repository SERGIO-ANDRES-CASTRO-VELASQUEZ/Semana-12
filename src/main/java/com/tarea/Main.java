package com.tarea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.P

/**
 * Clase que representa a un paciente en el sistema de urgencias
 */
class Paciente {
    private static int contadorId = 1;

    private final int id;
    private final String nombre;
    private final int prioridad; // 1=Rojo, 2=Amarillo, 3=Verde
    private final LocalDateTime horaLlegada;
    private final String sintomas;

    public Paciente(String nombre, int prioridad, String sintomas) {
        if (prioridad < 1 || prioridad > 3) {
            throw new IllegalArgumentException("Prioridad debe ser 1, 2 o 3");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        this.id = contadorId++;
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.sintomas = sintomas != null ? sintomas : "No especificado";
        this.horaLlegada = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public LocalDateTime getHoraLlegada() {
        return horaLlegada;
    }

    public String getSintomas() {
        return sintomas;
    }

    public String getNivelPrioridadTexto() {
        switch (prioridad) {
            case 1: return "ROJO (Emergencia)";
            case 2: return "AMARILLO (Urgente)";
            case 3: return "VERDE (No urgente)";
            default: return "Desconocido";
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[ID:%d] %s - %s - Llegada: %s - Síntomas: %s",
                id, nombre, getNivelPrioridadTexto(),
                horaLlegada.format(formatter), sintomas);
    }
}

/**
 * Sistema de Triage de Urgencias
 */
class SistemaTriageUrgencias {
    // Cola de prioridad con comparador personalizado
    private final PriorityQueue<Paciente> colaPacientes;

    // Historial de pacientes atendidos (para el extra de deshacer)
    private final Stack<Paciente> historialAtendidos;

    // Listas para reportes
    private final List<Paciente> listaAtendidos;

    // Contadores por prioridad
    private int contadorRojo;    // Prioridad 1
    private int contadorAmarillo; // Prioridad 2
    private int contadorVerde;    // Prioridad 3

    public SistemaTriageUrgencias() {
        // Comparador: primero por prioridad (ascendente),
        // luego por hora de llegada (el más antiguo primero)
        Comparator<Paciente> comparadorTriage = (p1, p2) -> {
            int comparacionPrioridad = Integer.compare(p1.getPrioridad(), p2.getPrioridad());
            if (comparacionPrioridad != 0) {
                return comparacionPrioridad;
            }
            // Desempate por hora de llegada (más temprano = mayor prioridad)
            return p1.getHoraLlegada().compareTo(p2.getHoraLlegada());
        };

        this.colaPacientes = new PriorityQueue<>(comparadorTriage);
        this.historialAtendidos = new Stack<>();
        this.listaAtendidos = new ArrayList<>();
        this.contadorRojo = 0;
        this.contadorAmarillo = 0;
        this.contadorVerde = 0;
    }

    /**
     * Registra la llegada de un nuevo paciente
     */
    public void registrarPaciente(String nombre, int prioridad, String sintomas) {
        try {
            Paciente paciente = new Paciente(nombre, prioridad, sintomas);
            colaPacientes.offer(paciente);

            // Actualizar contador según prioridad
            switch (prioridad) {
                case 1: contadorRojo++; break;
                case 2: contadorAmarillo++; break;
                case 3: contadorVerde++; break;
            }

            System.out.println("✓ Paciente registrado exitosamente:");
            System.out.println("  " + paciente);
            System.out.println();
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Error al registrar paciente: " + e.getMessage());
        }
    }

    /**
     * Ver el siguiente paciente a atender sin sacarlo de la cola
     */
    public void verSiguiente() {
        Paciente siguiente = colaPacientes.peek();

        if (siguiente == null) {
            System.out.println("ℹ No hay pacientes en espera.");
        } else {
            System.out.println("→ Siguiente paciente a atender:");
            System.out.println("  " + siguiente);
        }
        System.out.println();
    }

    /**
     * Atiende al siguiente paciente según prioridad triage
     */
    public void atender() {
        Paciente paciente = colaPacientes.poll();

        if (paciente == null) {
            System.out.println("ℹ No hay pacientes para atender.");
            System.out.println();
            return;
        }

        // Actualizar contadores
        switch (paciente.getPrioridad()) {
            case 1: contadorRojo--; break;
            case 2: contadorAmarillo--; break;
            case 3: contadorVerde--; break;
        }

        // Guardar en historial
        historialAtendidos.push(paciente);
        listaAtendidos.add(paciente);

        System.out.println("✓ Paciente atendido:");
        System.out.println("  " + paciente);
        System.out.println();
    }

    /**
     * Muestra los contadores por nivel de prioridad
     */
    public void mostrarContadores() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  CONTADORES POR NIVEL DE PRIORIDAD");
        System.out.println("═══════════════════════════════════════");
        System.out.println("  🔴 ROJO (Emergencia):    " + contadorRojo + " paciente(s)");
        System.out.println("  🟡 AMARILLO (Urgente):   " + contadorAmarillo + " paciente(s)");
        System.out.println("  🟢 VERDE (No urgente):   " + contadorVerde + " paciente(s)");
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  TOTAL EN ESPERA:         " + (contadorRojo + contadorAmarillo + contadorVerde));
        System.out.println("═══════════════════════════════════════");
        System.out.println();
    }

    /**
     * EXTRA: Deshace la última atención (reinserta al paciente)
     */
    public void deshacerUltimaAtencion() {
        if (historialAtendidos.isEmpty()) {
            System.out.println("ℹ No hay atenciones para deshacer.");
            System.out.println();
            return;
        }

        Paciente paciente = historialAtendidos.pop();
        colaPacientes.offer(paciente);
        listaAtendidos.remove(listaAtendidos.size() - 1);

        // Actualizar contadores
        switch (paciente.getPrioridad()) {
            case 1: contadorRojo++; break;
            case 2: contadorAmarillo++; break;
            case 3: contadorVerde++; break;
        }

        System.out.println("↶ Atención deshecha. Paciente reinsertado:");
        System.out.println("  " + paciente);
        System.out.println();
    }

    /**
     * EXTRA: Genera un reporte completo del sistema
     */
    public void generarReporte() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           REPORTE DEL SISTEMA DE TRIAGE - URGENCIAS          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

        // Pacientes atendidos
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│  PACIENTES ATENDIDOS: " + listaAtendidos.size() + "          │");
        System.out.println("└─────────────────────────────────────┘");
        if (listaAtendidos.isEmpty()) {
            System.out.println("  (Ninguno)");
        } else {
            for (Paciente p : listaAtendidos) {
                System.out.println("  " + p);
            }
        }
        System.out.println();

        // Pacientes en espera
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│  PACIENTES EN ESPERA: " + colaPacientes.size() + "           │");
        System.out.println("└─────────────────────────────────────┘");
        if (colaPacientes.isEmpty()) {
            System.out.println("  (Ninguno)");
        } else {
            // Copiar cola para no modificarla
            List<Paciente> enEspera = new ArrayList<>(colaPacientes);
            enEspera.sort((p1, p2) -> {
                int cmp = Integer.compare(p1.getPrioridad(), p2.getPrioridad());
                return cmp != 0 ? cmp : p1.getHoraLlegada().compareTo(p2.getHoraLlegada());
            });
            for (Paciente p : enEspera) {
                System.out.println("  " + p);
            }
        }
        System.out.println();

        // Contadores
        mostrarContadores();
    }

    /**
     * Muestra todos los pacientes en espera ordenados
     */
    public void listarPacientesEnEspera() {
        if (colaPacientes.isEmpty()) {
            System.out.println("ℹ No hay pacientes en espera.");
            System.out.println();
            return;
        }

        System.out.println("═══════════════════════════════════════");
        System.out.println("  PACIENTES EN SALA DE ESPERA");
        System.out.println("═══════════════════════════════════════");

        List<Paciente> lista = new ArrayList<>(colaPacientes);
        lista.sort((p1, p2) -> {
            int cmp = Integer.compare(p1.getPrioridad(), p2.getPrioridad());
            return cmp != 0 ? cmp : p1.getHoraLlegada().compareTo(p2.getHoraLlegada());
        });

        for (int i = 0; i < lista.size(); i++) {
            System.out.println((i + 1) + ". " + lista.get(i));
        }
        System.out.println("═══════════════════════════════════════");
        System.out.println();
    }
}

/**
 * Clase principal con menú interactivo
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SistemaTriageUrgencias sistema = new SistemaTriageUrgencias();

        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║       SISTEMA DE TRIAGE - SALA DE URGENCIAS HOSPITALARIA     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

        // Cargar datos de ejemplo para demostración
        cargarDatosEjemplo(sistema);

        boolean continuar = true;

        while (continuar) {
            mostrarMenu();
            int opcion = leerOpcion();
            System.out.println();

            switch (opcion) {
                case 1:
                    registrarNuevoPaciente(sistema);
                    break;
                case 2:
                    sistema.verSiguiente();
                    break;
                case 3:
                    sistema.atender();
                    break;
                case 4:
                    sistema.mostrarContadores();
                    break;
                case 5:
                    sistema.listarPacientesEnEspera();
                    break;
                case 6:
                    sistema.deshacerUltimaAtencion();
                    break;
                case 7:
                    sistema.generarReporte();
                    break;
                case 0:
                    System.out.println("Cerrando sistema de triage. ¡Hasta pronto!");
                    continuar = false;
                    break;
                default:
                    System.out.println("✗ Opción inválida. Intente nuevamente.\n");
            }

            if (continuar && opcion != 0) {
                esperarEnter();
            }
        }

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("┌───────────────────────────────────────────────────────────┐");
        System.out.println("│                      MENÚ PRINCIPAL                       │");
        System.out.println("├───────────────────────────────────────────────────────────┤");
        System.out.println("│  1. Registrar nuevo paciente                              │");
        System.out.println("│  2. Ver siguiente paciente a atender                      │");
        System.out.println("│  3. Atender paciente                                      │");
        System.out.println("│  4. Mostrar contadores por prioridad                      │");
        System.out.println("│  5. Listar todos los pacientes en espera                  │");
        System.out.println("│  6. Deshacer última atención (EXTRA)                      │");
        System.out.println("│  7. Generar reporte completo (EXTRA)                      │");
        System.out.println("│  0. Salir                                                 │");
        System.out.println("└───────────────────────────────────────────────────────────┘");
        System.out.print("Seleccione una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void registrarNuevoPaciente(SistemaTriageUrgencias sistema) {
        System.out.println("─── REGISTRO DE NUEVO PACIENTE ───");

        System.out.print("Nombre del paciente: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("Prioridad (1=ROJO/Emergencia, 2=AMARILLO/Urgente, 3=VERDE/No urgente): ");
        int prioridad;
        try {
            prioridad = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("✗ Prioridad inválida. Usando prioridad 3 (VERDE) por defecto.");
            prioridad = 3;
        }

        System.out.print("Síntomas: ");
        String sintomas = scanner.nextLine().trim();

        System.out.println();
        sistema.registrarPaciente(nombre, prioridad, sintomas);
    }

    private static void cargarDatosEjemplo(SistemaTriageUrgencias sistema) {
        System.out.println("Cargando datos de ejemplo...\n");

        sistema.registrarPaciente("Carlos Méndez", 2, "Dolor abdominal intenso");
        sistema.registrarPaciente("Ana García", 1, "Paro cardíaco");
        sistema.registrarPaciente("Luis Rodríguez", 3, "Resfriado común");
        sistema.registrarPaciente("María López", 1, "Trauma craneal severo");
        sistema.registrarPaciente("Pedro Sánchez", 2, "Fractura de brazo");
        sistema.registrarPaciente("Sofía Torres", 3, "Consulta de rutina");

        System.out.println("═══════════════════════════════════════\n");
    }

    private static void esperarEnter() {
        System.out.print("\nPresione ENTER para continuar...");
        scanner.nextLine();
        System.out.println();
    }
}