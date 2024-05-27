package org.example;

import ru.pflb.mq.dummy.implementation.ConnectionImpl;
import ru.pflb.mq.dummy.interfaces.Destination;
import ru.pflb.mq.dummy.interfaces.Producer;
import ru.pflb.mq.dummy.interfaces.Session;
import ru.pflb.mq.dummy.exception.DummyException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/*P.S. Насколько я понял из ТЗ, построчный вывод текста из файла должен быть аналогичным 1 заданию,
т.е через каждые 2с по строчке в сопровождении даты и "- Отправляю сообщение:"*/
public class Task2 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("В аргументе командной строки. необходимо указать путь до файла messages.dat");
            return;
        }
        String filePath = args[0];
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Создаем соединение
            ConnectionImpl connection = new ConnectionImpl();
            /*Запускаю соединение(На самом деле имитириую, что запускаем). Не знаю надо ли это в коде оставлять.
            Согласно примеру результата из ДЗ нет. Но я решил оставить*/
            connection.start();
            // Создаем сессию с подтверждением
            Session session = connection.createSession(true);
            // Создаю назначение(название) очереди
            Destination destination = session.createDestination("barbecue");
            // Создаю объект  для отправки сообщений в очередь
            Producer producer = session.createProducer(destination);
            // Отправляем строки из файла в очередь
            String line;
            while ((line = reader.readLine()) != null) {
                Thread.sleep(2000);
                producer.send(line);
            }
            // Закрываем сессию и соединение
            session.close();
            connection.close();
        } catch (DummyException | InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}