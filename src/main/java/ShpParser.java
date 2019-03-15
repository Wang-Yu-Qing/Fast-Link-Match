import com.vividsolutions.jts.geom.*;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.*;

public class ShpParser {
    public FeatureCollection<SimpleFeatureType, SimpleFeature> shp_feature_collection;
    public Map<Point, ArrayList<Map<String, Geometry>>> node_link_relation = new HashMap<>(); // store each node's contains links
    public GeometryFactory gf = new GeometryFactory();


    public Map<String, Double> find_max_min_lon_lat_in_one_link(Coordinate[] link_corners){
        Map<String, Double> result = new HashMap<>();
        // max lon and max lat may from two different points
        double max_lon = Double.NEGATIVE_INFINITY;
        double max_lat = Double.NEGATIVE_INFINITY;
        double min_lon = Double.POSITIVE_INFINITY;
        double min_lat = Double.POSITIVE_INFINITY;
        for (Coordinate corner:link_corners){
            if (corner.x > max_lon) {
                max_lon = corner.x;
            }
            if (corner.x < min_lon) {
                min_lon = corner.x;
            }
            if (corner.y > max_lat) {
                max_lat = corner.y;
            }
            if (corner.y < min_lat) {
                min_lat = corner.y;
            }
        }
        result.put("max_lon", max_lon);
        result.put("max_lat", max_lat);
        result.put("min_lon", min_lon);
        result.put("min_lat", min_lat);
        return result;
    }

    public Map<String, Double> parse_shp_file(String file_path) throws IOException {
        Map<String, Double> overall_max_min_lon_lat = new HashMap<>();
        ArrayList<Double> lons = new ArrayList<>();
        ArrayList<Double> lats = new ArrayList<>();
        // read external link shp file
        ShapefileDataStore shpDataStore = new ShapefileDataStore(new File(file_path).toURI().toURL());
        shpDataStore.setCharset(Charset.forName("GBK"));
        String typeName = shpDataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
        featureSource = shpDataStore.getFeatureSource(typeName);
        this.shp_feature_collection = featureSource.getFeatures();
        shpDataStore.dispose();
        FeatureIterator<SimpleFeature> link_iter = this.shp_feature_collection.features();
        while (link_iter.hasNext()) {
            SimpleFeature link_feature = link_iter.next();
            Geometry link_geometry = (Geometry) link_feature.getDefaultGeometry();
            assert link_geometry instanceof com.vividsolutions.jts.geom.MultiLineString;
            // find max min lat lon for this link
            Coordinate[] link_corners = link_geometry.getCoordinates();
            Map<String, Double> max_min_lon_lat = find_max_min_lon_lat_in_one_link(link_corners);
            lons.add(max_min_lon_lat.get("max_lon"));
            lats.add(max_min_lon_lat.get("max_lat"));
            lons.add(max_min_lon_lat.get("min_lon"));
            lats.add(max_min_lon_lat.get("min_lat"));
            // update node-links relations
            for (Coordinate cor: link_corners) {
                Point node = gf.createPoint(cor);
                HashMap<String, Geometry> linkID_geom = new HashMap<>();
                linkID_geom.put((link_feature.getAttribute("ID")).toString(), link_geometry);
                if (this.node_link_relation.containsKey(node)) {
                    this.node_link_relation.get(node).add(linkID_geom);
                }
                else {
                    this.node_link_relation.put(node, new ArrayList<>(Arrays.asList(linkID_geom)));
                }
            }
        }
        link_iter.close();
        overall_max_min_lon_lat.put("max_lon", Collections.max(lons));
        overall_max_min_lon_lat.put("max_lat", Collections.max(lats));
        overall_max_min_lon_lat.put("min_lon", Collections.min(lons));
        overall_max_min_lon_lat.put("min_lat", Collections.min(lats));
        return overall_max_min_lon_lat;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getLinkFeatureCollection() {
        return this.shp_feature_collection;
    }

    public Map<Point, ArrayList<Map<String, Geometry>>> getNodeLinkRelation() {
        return this.node_link_relation;
    }
}
