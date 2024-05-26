package org.example;

import ru.pflb.mq.dummy.implementation.ConnectionImpl;
import ru.pflb.mq.dummy.interfaces.Connection;
import ru.pflb.mq.dummy.interfaces.Destination;
import ru.pflb.mq.dummy.interfaces.Producer;
import ru.pflb.mq.dummy.interfaces.Session;
import ru.pflb.mq.dummy.exception.DummyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/* P.S. Насколько я понял из ТЗ, построчный вывод текста из файла должен быть аналогичным 1 заданию,
т.е через каждые 2с по строчке в сопровождении даты и "- Отправляю сообщение:" */
public class Task2Loop {
    private static volatile boolean KEEPRUNNING = true;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("В аргументе командной строки необходимо указать путь до файла messages.dat");
            return;
        }
        String filePath = args[0];
        Thread controlThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (KEEPRUNNING) {
                System.out.println("Введите 'q' для завершения программы:");
                String input = scanner.nextLine();
                if ("q".equalsIgnoreCase(input)) {
                    KEEPRUNNING = false;
                }
            }
        });

        controlThread.start();

        try {
            // Создаю соединение
            Connection connection = new ConnectionImpl();
            connection.start();
            // Создаею сессию с подтверждением
            Session session = connection.createSession(true);
            // Создаю назначение (очередь) с понятным именем
            Destination destination = session.createDestination("barbecue");
            // Создаю продюсера для отправки сообщений в очередь
            Producer producer = session.createProducer(destination);

            // Отправляю строки из файла в бесконечную очередь
            while (KEEPRUNNING) {
                resetToBeginning(filePath, producer);
            }

            // Закрытие сессии и соединения
            session.close();
            connection.close();
        } catch (DummyException e) {
            e.printStackTrace();
        }

        try {
            controlThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void resetToBeginning(String filePath, Producer producer) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while (KEEPRUNNING && (line = reader.readLine()) != null) {
                Thread.sleep(2000);
                producer.send(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}