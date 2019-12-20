import java.io.*;
import java.util.*;

public class subway {

    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(args));
        Map<String, List<String>> coml = new HashMap<>(); //将指令台读入的参数转为Map的形式
        List<String> pre_list = new ArrayList<>();
        String pre_key = "";
        for (String _cur_str : args) {
            if (_cur_str.startsWith("-")) {
                pre_list = new ArrayList<>();
                pre_key = _cur_str;
                coml.put(_cur_str, pre_list);
            } else {
                if (!coml.containsKey(pre_key)) {
                    coml.put(pre_key, new ArrayList<>());
                }
                coml.get(pre_key).add(_cur_str);
            }
        }
        System.out.println(coml);
        HashMap<String, List<String>> subwayMap = readMap(coml.get("-map").get(0));

//        对于需求一和需求二的两个指令格式进行不同的处理
        if (coml.containsKey("-b")) {
            if (!mapContainSta(subwayMap, coml.get("-b").get(0)) || !mapContainSta(subwayMap, coml.get("-b").get(1))) {
                String zt = "";
                if(!mapContainSta(subwayMap, coml.get("-b").get(0))){zt = coml.get("-b").get(0);}
                else{zt = coml.get("-b").get(1);}
                saveRoutes2File(Arrays.asList("站台:" + zt + " 不存在"), coml.get("-o").get(0));
                System.out.println("站台:" + zt + " 不存在");
            } else {
                List<String> min_path = bfs(subwayMap, coml.get("-b").get(0), coml.get("-b").get(1));
                assert min_path != null;
                List<String> routes = generateRoute(subwayMap, min_path);
//            去除几号线
                int k = 0;
                for (String r : routes) {
                    if (r.endsWith("号线")) {
                        k++;
                    }
                }
                routes.add(0, String.valueOf(routes.size() - k));
                routes.stream().forEach(x -> System.out.println(x));
                saveRoutes2File(routes, coml.get("-o").get(0));
            }
        } else if (coml.containsKey("-a")) {
            String li = coml.get("-a").get(0);
            if (!mapContainsLine(subwayMap, coml.get("-a").get(0))) {
                System.out.println("线路:" + coml.get("-a").get(0) + " 不存在");
                saveRoutes2File(Arrays.asList("线路:" + coml.get("-a").get(0) + "不存在"), coml.get("-o").get(0));
            } else {
                saveRoutes2File(subwayMap.get(li), coml.get("-o").get(0));
            }
        }
    }

    private static boolean mapContainsLine(HashMap<String, List<String>> subwayMap, String line) {
        return subwayMap.keySet().contains(line);
    }

    private static boolean mapContainSta(HashMap<String, List<String>> subwayMap, String sta) {
        for (String k : subwayMap.keySet()) {
            List<String> list = subwayMap.get(k);
            if (list.contains(sta)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存路径到文件中
     *
     * @param routes 路径
     * @param path   文件保存路径
     * @throws IOException
     */
    private static void saveRoutes2File(List<String> routes, String path) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
        for (String cur : routes) {
            bw.write(cur);
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    /**
     * 用于封装临时返回结果的类
     */
    static class TempRes {
        boolean inOneLine = false;
        String nextLine = "";
        public TempRes(boolean inOneLine, String nextLine) {
            this.inOneLine = inOneLine;
            this.nextLine = nextLine;
        }
    }

    /**
     * 通过bfs的结果生成最终的路径
     *
     * @param subwayMap 地图
     * @param min_path  最小路径
     * @return bfs生成的最小路径
     */
    private static List<String> generateRoute(HashMap<String, List<String>> subwayMap, List<String> min_path) {
        String curLine = whichLine(subwayMap, min_path.get(0));
        List<String> routers = new ArrayList<>();
        routers.add(min_path.get(0));
        for (int idx = 0; idx < min_path.size() - 1; idx++) {
            String cur_s = min_path.get(idx);
            String next_s = min_path.get(idx + 1);
            assert !cur_s.equals(next_s);
            TempRes tr = is_one_line(subwayMap, curLine, cur_s, next_s);
            if (tr.inOneLine) {
                routers.add(next_s);
            } else {
                curLine = tr.nextLine;
                routers.add(tr.nextLine);
                routers.add(next_s);
            }
        }
        return routers;
    }

    /**
     * 判断两个站点是否在一个线路上
     *
     * @param subwayMap 地图
     * @param curLine   当前处于 几号线上
     * @param cur_s     当前站点
     * @param next_s    需要移动到的站点
     * @return 结果
     */
    private static TempRes is_one_line(HashMap<String, List<String>> subwayMap, String curLine, String cur_s, String next_s) {
        TempRes res = new TempRes(false, "");
//        获取当前线路，是否包含下一个站点
        List<String> curStations = subwayMap.get(curLine);
        String pre, cur;
        for (int i = 1; i < curStations.size(); i++) {
            pre = curStations.get(i - 1);
            cur = curStations.get(i);
            if (pre.equals(cur_s) && cur.equals(next_s) || pre.equals(next_s) && cur.equals(cur_s)) {
                return new TempRes(true, "");
            }
        }
        for (String line : subwayMap.keySet()) {
            if (line.equals(curLine)) {
                continue;
            }
            curStations = subwayMap.get(line);
//            需要查出前后关系
            for (int i = 1; i < curStations.size(); i++) {
                pre = curStations.get(i - 1);
                cur = curStations.get(i);
                if (pre.equals(cur_s) && cur.equals(next_s) || pre.equals(next_s) && cur.equals(cur_s)) {
                    return new TempRes(false, line);
                }
            }
        }
        assert true : "一定能够判断出当前站点和站点之间的关系";
        return null;
    }

    /**
     * 当前站点在哪个站点上
     *
     * @param subwayMap
     * @param station
     * @return
     */
    private static String whichLine(HashMap<String, List<String>> subwayMap, String station) {
        for (String line : subwayMap.keySet()) {
            List<String> stations = subwayMap.get(line);
            for (String st : stations) {
                if (st.equals(station)) {
                    return line;
                }
            }
        }
        assert true;
        return "";
    }

    /**
     * 进行bfs搜索，将整个地图看成一张图，并不去关心当前站点处于哪条线路上
     *
     * @param subwayMap 地图
     * @param from      开始的站点
     * @param to        结束的站点
     * @return
     */
    private static List<String> bfs(HashMap<String, List<String>> subwayMap, String from, String to) {
        int min_path_len = Integer.MAX_VALUE;
        List<String> min_paths = null;
        LinkedList<List<String>> paths = new LinkedList<>(); //当前遍历的全部路径，使用的bfs算法，所以把paths当作队列来使用
        List<String> initalPath = new ArrayList<>();//初始化路径，只包含from
        List<List<String>> needRemove = new ArrayList<>();
        initalPath.add(from);
//        添加到队尾
        paths.addLast(initalPath);
        boolean end = false;
        while (paths.size() > 0 && !end) {
            int tempSize = paths.size(); // 保存当前需要处理的队列代下
            while (tempSize > 0) {
                tempSize--;
                List<String> difPath = paths.get(0);
                String cur_s = difPath.get(difPath.size() - 1);
                List<String> curPath = difPath;
                if (cur_s.equals(to)) {//如果达到终点
                    if (curPath.size() < min_path_len) {
                        min_path_len = curPath.size();
                        min_paths = curPath;
                        end = true;
                    }
                } else {
                    paths.removeFirst();//去除原来旧的元素，并基于它生成新的路径
                    for (String next_s : next_s_all(cur_s, subwayMap)) {
                        if (curPath.contains(next_s)) {
                            continue;
                        }
                        List<String> new_path = new ArrayList<>(curPath);
                        new_path.add(next_s);
                        paths.add(new_path);
                    }
                }
            }
        }
        return min_paths;
    }

    /**
     * 判断节点相邻的值
     *
     * @param cur_station 当前站点
     * @param subwayMap   地图
     * @return
     */
    private static Set<String> next_s_all(String cur_station, HashMap<String, List<String>> subwayMap) {
//        注意这里的结果是否正确
        Set<String> next_sets = new HashSet<>();
//        检查这条线路上相邻的站点
        for (String lin : subwayMap.keySet()) {
            List<String> curLineStations = subwayMap.get(lin);
            int curSIdx = curLineStations.indexOf(cur_station);
            if (curSIdx < 0) {
                continue;
            }
            /*添加相邻的点做为下一次可以到达的点*/
            if (curSIdx - 1 >= 0) {
                next_sets.add(curLineStations.get(curSIdx - 1));
            }
            if (curSIdx + 1 <= curLineStations.size() - 1) {
                next_sets.add(curLineStations.get(curSIdx + 1));
            }
        }
        return next_sets;
    }

    /**
     * 从文件中读取地图信息
     * 读取文件的方法直接和文件的格式有关系，尽量不要轻易修改文件的格式
     *
     * @param path 文件路径
     * @return 返回地图
     * @throws IOException
     */
    private static HashMap<String, List<String>> readMap(String path) throws IOException {
        HashMap<String, List<String>> resMap = new HashMap<>();
        String _path = subway.class.getResource("/" + path).getPath(); //这个路径不太正确需要去掉前面的/
        _path = _path.substring(1, _path.length());
        System.out.println("path : " + _path);
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(_path), "UTF-8"
        ));
        String lineText = "";
        while ((lineText = bf.readLine()) != null) {
            int quote_idx = lineText.indexOf("：");
            String li = lineText.substring(0, quote_idx);
            List<String> stations = new ArrayList<>(Arrays.asList(lineText.substring(quote_idx + 1, lineText.length()).split("，")));
            resMap.put(li, stations);
        }
        bf.close();
        return resMap;
    }
}
