package ru.spbu.astro.db;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import ru.spbu.astro.model.Point;

import java.sql.*;
import java.util.*;

public class SQLPointDepot implements PointDepot {

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
    public Map<Integer, Point> get(Collection<Integer> ids) {
        String query = "(";
        for (Integer id : ids) {
            query += id + ", ";
        }
        query = query.substring(0, query.length() - 2);
        query += ")";

        List<Coordinate> coordinateList = simpleJdbcTemplate.query(
                "SELECT * FROM point WHERE point_id IN " + query,
                getRowMapper()
        );

        Map<Integer, List<Coordinate>> id2coordinates = new HashMap();

        for (Coordinate coordinate : coordinateList) {
            if (!id2coordinates.containsKey(coordinate.getPointId())) {
                List<Coordinate> coordinates = new ArrayList();
                coordinates.add(coordinate);
                id2coordinates.put(coordinate.getPointId(), coordinates);
            } else {
                List<Coordinate> coordinates = id2coordinates.get(coordinate.getPointId());
                coordinates.add(coordinate);
                id2coordinates.put(coordinate.getPointId(), coordinates);
            }
        }

        Map<Integer, Point> result = new HashMap();

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
    public List<Integer> add(List<Point> points) {
        return null;
    }

    @Override
    public void drop() {
        simpleJdbcTemplate.update("DELETE FROM point");
    }

    private static Point makePoint(List<Coordinate> coordinates) {
        int maxIndex = 0;
        for (Coordinate coordinate : coordinates) {
            maxIndex = Math.max(maxIndex, coordinate.getIndex());
        }

        Point result = new Point(maxIndex + 1);
        for (Coordinate coordinate : coordinates) {
            result.set(coordinate.getIndex(), coordinate.getValue());
        }

        return result;
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

        private int getPointId() {
            return pointId;
        }

        private void setPointId(int pointId) {
            this.pointId = pointId;
        }

        private int getIndex() {
            return index;
        }

        private void setIndex(int index) {
            this.index = index;
        }

        private long getValue() {
            return value;
        }

        private void setValue(long value) {
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
        //System.out.println("!!!");
        this.simpleJdbcTemplate = simpleJdbcTemplate;
    }
}
