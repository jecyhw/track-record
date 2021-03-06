package cn.cnic.trackrecord.core.track.xml;

import cn.cnic.trackrecord.common.util.Objects;
import cn.cnic.trackrecord.common.xml.Stax.StaxHandler;
import cn.cnic.trackrecord.common.xml.Stax.Staxs;
import cn.cnic.trackrecord.data.entity.TrackPoint;
import cn.cnic.trackrecord.data.kml.PlaceMarkBuilder;
import cn.cnic.trackrecord.data.kml.PlaceMarkType;
import cn.cnic.trackrecord.data.kml.RouteRecord;
import cn.cnic.trackrecord.data.kml.RouteStyle;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RouteRecord.kml文件解析。继承
 * @see cn.cnic.trackrecord.common.xml.Stax.StaxHandler 在解析。
 * 一个RouteRecord.kml文件存在两种
 * @see cn.cnic.trackrecord.data.kml.PlaceMark 标注，分别是
 * 关键点坐标信息标注
 * @see cn.cnic.trackrecord.data.kml.KeyPointPlaceMark 和
 * 轨迹坐标点信息标注
 * @see cn.cnic.trackrecord.data.kml.RoutePlaceMark
 */
@Slf4j
public class RouteRecordXml extends StaxHandler<RouteRecord> {
    private RouteRecordBuilder routeRecordBuilder = new RouteRecordBuilder();

    @Override
    public void parse() throws XMLStreamException {
        while (hasNext()) {
            XMLEvent xmlEvent = nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("Placemark")) {
                    routeRecordBuilder.startPlaceMark();
                } else if (startElement.getName().getLocalPart().equals("LineString")) {
                    routeRecordBuilder.placeMarkType(PlaceMarkType.ROUTE);
                } else if (startElement.getName().getLocalPart().equals("Style")) {
                    Attribute attr = startElement.getAttributeByName(new QName("id"));
                    routeRecordBuilder.style(Objects.nonNull(attr) ? attr.getValue() : null);// id可能为空
                } else if (startElement.getName().getLocalPart().equals("coordinates")) {
                    routeRecordBuilder.coordinates(nextData());
                } else if (startElement.getName().getLocalPart().equals("name")) {
                    routeRecordBuilder.name(nextData());
                }  else if (startElement.getName().getLocalPart().equals("description")) {
                    routeRecordBuilder.desc(nextData());
                } else if (startElement.getName().getLocalPart().equals("color")) {
                    routeRecordBuilder.routeStyleColor(nextData());
                }  else if (startElement.getName().getLocalPart().equals("width")) {
                    routeRecordBuilder.routeStyleWidth(nextData());
                }  else if (startElement.getName().getLocalPart().equals("styleUrl")) {
                    routeRecordBuilder.styleUrl(nextData());
                }
            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("Placemark")) {
                    routeRecordBuilder.endPlaceMark();
                }
            }
        }
    }

    @Override
    public RouteRecord getResult() {
        return routeRecordBuilder.build();
    }

    static private class RouteRecordBuilder {
        /**
         * 匹配图片、音视频标签
         */
        private static Pattern descPatten = Pattern.compile("(img|video|audio)\\s+src=\"([^\"]+)\"");
        /**
         * 用来保存RouteRecord.xml文件的所有Style，key为Style的id
         *
         * Style是轨迹路径的样式，一个RouteRecord.xml下可能会存在多条路线，每条Style不一样。
         */
        private Map<String, RouteStyle> routeStyleMap = new HashMap<>();
        private RouteStyle routeStyle;

        private List<PlaceMarkBuilder> placeMarkBuilders = new LinkedList<>();
        private PlaceMarkBuilder placeMarkBuilder;

        RouteRecord build() {
            RouteRecord routeRecord = new RouteRecord();
            for (PlaceMarkBuilder placeMarkBuilder : placeMarkBuilders) {
                RouteStyle routeStyle = placeMarkBuilder.routeStyle();
                if (Objects.nonNull(routeStyle) && Objects.nonNull(routeStyle.getId())) {
                    // routeStyle的id以#开头，需要替换掉再转成数字
                    placeMarkBuilder.routeStyle(routeStyleMap.get(routeStyle.getId().replaceFirst("^#+", "")));
                }
                routeRecord.getPlaceMarks().add(placeMarkBuilder.build());
            }
            return routeRecord;
        }

        void startPlaceMark() {
            placeMarkBuilder = new PlaceMarkBuilder();
            placeMarkBuilder.placeMarkType(PlaceMarkType.KEYPOINT);
        }

        void endPlaceMark() {
            placeMarkBuilders.add(placeMarkBuilder);
            placeMarkBuilder = null;
        }

        void style(String id) {
            routeStyle = new RouteStyle();
            routeStyle.setId(id);
            routeStyleMap.put(id, routeStyle);

            if (Objects.nonNull(placeMarkBuilder)) {
                placeMarkBuilder.routeStyle(routeStyle);
            }
        }

        void placeMarkType(PlaceMarkType type) {
            placeMarkBuilder.placeMarkType(type);
        }

        void coordinates(String pointsText) {
            switch (placeMarkBuilder.placeMarkType()) {
                case KEYPOINT:
                    placeMarkBuilder.point(trackPoint(pointsText));
                    break;
                case ROUTE:
                    placeMarkBuilder.route(trackPoints(pointsText));
                    break;
            }
        }

        TrackPoint trackPoint(String pointText) {
            TrackPoint point = new TrackPoint();
            //坐标点信息以,分隔
            StringTokenizer tokenizer = new StringTokenizer(pointText, ",", false);
            if (tokenizer.hasMoreTokens())
            {
                point.setLng(Double.parseDouble(tokenizer.nextToken()));
                point.setLat(Double.parseDouble(tokenizer.nextToken()));
                point.setAltitude(Double.parseDouble(tokenizer.nextToken()));
            }
            return point;
        }

        List<TrackPoint> trackPoints(String pointsText) {
            List<TrackPoint> points = new LinkedList<>();
            //坐标点之间以空格分隔
            StringTokenizer tokenizer = new StringTokenizer(pointsText, " ", false);
            while (tokenizer.hasMoreTokens()) {
                points.add(trackPoint(tokenizer.nextToken()));
            }
            return points;
        }

        void name(String name) {
            placeMarkBuilder.name(name);
        }

        void routeStyleColor(String color) {
            routeStyle.setColor(color);
        }

        void routeStyleWidth(String text) {
            routeStyle.setWidth(text);
        }

        void styleUrl(String id) {
            RouteStyle routeStyle = new RouteStyle();
            routeStyle.setId(id);
            placeMarkBuilder.routeStyle(routeStyle);
        }

        void desc(String descText) {
            List<String> desc = new LinkedList<>();
            Matcher matcher = descPatten.matcher(descText);
            while (matcher.find()) {
                // 一个src下的多个图片、音视频地址之间以,分隔。matcher.group(2)是取得src的内容
                desc.addAll(Arrays.asList(matcher.group(2).split(",")));
            }
            placeMarkBuilder.desc(desc);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        System.out.println(Staxs.parse(new RouteRecordXml(), "G:\\gpstracks\\123.xml"));
    }
}
