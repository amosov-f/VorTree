package ru.spbu.astro.db;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import ru.spbu.astro.model.Point;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SQLPointDepot implements PointDepot {

    private SimpleJdbcTemplate simpleJdbcTemplate;

    @Override
    public Point get(int id) {
        List<Coordinate> coordinates = simpleJdbcTemplate.query(
                "SELECT * FROM point WHERE point_id = ?",
                getRowMapper(),
                id
        );

        return makePoint(coordinates);
    }

    @Override
    public Map<Integer, Point> get(final Iterable<Integer> ids) {
        String query = "(";
        for (Integer id : ids) {
            query += id + ", ";
        }
        query = query.substring(0, query.length() - 2);
        query += ")";

        final List<Coordinate> coordinateList = simpleJdbcTemplate.query(
                "SELECT * FROM point WHERE point_id IN " + query,
                getRowMapper()
        );

        final Map<Integer, List<Coordinate>> id2coordinates = new HashMap<>();

        for (final Coordinate coordinate : coordinateList) {
            if (!id2coordinates.containsKey(coordinate.pointId)) {
                final List<Coordinate> coordinates = new ArrayList<>();
                coordinates.add(coordinate);
                id2coordinates.put(coordinate.pointId, coordinates);
            } else {
                final List<Coordinate> coordinates = id2coordinates.get(coordinate.pointId);
                coordinates.add(coordinate);
                id2coordinates.put(coordinate.pointId, coordinates);
            }
        }

        Map<Integer, Point> result = new HashMap<>();

        for (Map.Entry<Integer, List<Coordinate>> coordinatesEntry : id2coordinates.entrySet()) {
            result.put(coordinatesEntry.getKey(), makePoint(coordinatesEntry.getValue()));
        }

        return result;
    }

    @Override
    public int add(Point p) {
        if (p.dim() < 1) {
            return -1;
        }


        int id = simpleJdbcTemplate.queryForInt("SELECT MAX(point_id) FROM point") + 1;

        String query = "";
        for (int i = 0; i < p.dim(); ++i) {
            query += "(" + id + ", " + i + ", " + p.get(i) + ")";
            if (i < p.dim() - 1) {
                query += ", ";
            }
        }

        simpleJdbcTemplate.update("INSERT INTO point values " + query);

        return id;
    }

    @Override
    public List<Integer> add(final Iterable<Point> points) {
        List<Integer> ids = new ArrayList<>();
        for (final Point p : points) {
            ids.add(add(p));
        }
        return ids;
    }

    @Override
    public void clear() {
        simpleJdbcTemplate.update("DELETE FROM point");
    }

    private static Point makePoint(final Iterable<Coordinate> coordinates) {
        int maxIndex = 0;
        for (final Coordinate coordinate : coordinates) {
            maxIndex = Math.max(maxIndex, coordinate.index);
        }

        final long[] coordinateArray = new long[maxIndex + 1];
        for (final Coordinate coordinate : coordinates) {
            coordinateArray[coordinate.index] = coordinate.value;
        }

        return new Point(coordinateArray);
    }

    private class Coordinate {
        private int pointId;
        private int index;
        private long value;

        private Coordinate(int pointId, int index, long value) {
            this.pointId = pointId;
            this.index = index;
            this.value = value;
        }
    }
    
    private RowMapper<Coordinate> getRowMapper() {
        return new ParameterizedRowMapper<Coordinate>() {
            @Override
            public Coordinate mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Coordinate(
                        resultSet.getInt("point_id"),
                        resultSet.getInt("coordinate_index"),
                        resultSet.getLong("value")
                );
            }
        };
    }

    @Required
    public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
        this.simpleJdbcTemplate = simpleJdbcTemplate;
    }

}
