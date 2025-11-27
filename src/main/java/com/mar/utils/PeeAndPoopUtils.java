package com.mar.utils;

import com.mar.data.PeeAndPoopData;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class PeeAndPoopUtils {

    public static byte[] getChartImage(PeeAndPoopData data) {
        DefaultCategoryDataset barDataset = createBarDataset(data);
        DefaultCategoryDataset lineDataset = createLineDataset();
        JFreeChart chart = ChartFactory.createBarChart(
                "Pee&Poop", // заголовок
                "Day",                          // ось X
                "Count",               // ось Y
                barDataset,                     // данные для столбцов
                PlotOrientation.VERTICAL,       // ориентация
                true,                           // легенда
                true,                           // tooltips
                false                           // URLs
        );

        // Настраиваем график
        customizeChart(chart, barDataset, lineDataset);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsPNG(out, chart, 1000, 600);
        } catch (IOException e) {
            log.error("Cannot create chart image. ", e);
            return null;
        }
        return out.toByteArray();
    }

    private static int[] getArrayCopy7(int[] data) {
        int[] rez = new int[7];
        int i = 6;
        for (int j = data.length - 1; j >= 0; j--) {
            rez[i--] = data[j];
        }
        return rez;
    }

    private static DefaultCategoryDataset createBarDataset(PeeAndPoopData data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Пример данных
        int[] peeCount = getArrayCopy7(data.getLastWeekPee());
        int[] pooCount = getArrayCopy7(data.getLastWeekPoop());

        AtomicInteger i = new AtomicInteger(0);
        AtomicInteger j = new AtomicInteger(0);
        Stream.generate(() -> LocalDate.now().minusDays(i.getAndIncrement())).limit(7)
                .forEach(day -> {
                    int pee = peeCount[j.get()];
                    int poop = pooCount[j.getAndIncrement()];
                    dataset.addValue(pee, "Pee", day);
                    dataset.addValue(poop, "Poop", day);
                });

        return dataset;
    }

    private static DefaultCategoryDataset createLineDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Нормативные значения
        double normalPee = 6.0;
        double normalPoo = 2.0;

        AtomicInteger i = new AtomicInteger();
        Stream.generate(() -> LocalDate.now().minusDays(i.getAndIncrement())).limit(7)
                .forEach(day -> {
                    dataset.addValue(normalPee, "Normal Pee", day);
                    dataset.addValue(normalPoo, "Normal Poop", day);
                });

        return dataset;
    }

    private static void customizeChart(JFreeChart chart, DefaultCategoryDataset barDataset, DefaultCategoryDataset lineDataset) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);

        // Настраиваем рендерер для столбцов
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setSeriesPaint(0, new Color(65, 105, 225)); // синий для пописов
        barRenderer.setSeriesPaint(1, new Color(139, 69, 19));  // коричневый для покаков
        barRenderer.setItemMargin(0.1);

        // Настраиваем рендерер для линий
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.BLUE);           // линия нормы пописов
        lineRenderer.setSeriesPaint(1, new Color(139, 69, 19)); // линия нормы покаков
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
        lineRenderer.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
        lineRenderer.setSeriesShapesVisible(0, false);
        lineRenderer.setSeriesShapesVisible(1, false);

        // Устанавливаем оба рендерера на график
        plot.setRenderer(0, barRenderer);
        plot.setDataset(1, lineDataset);
        plot.setRenderer(1, lineRenderer);

        // Указываем порядок отрисовки (сначала столбцы, потом линии)
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    }

}
