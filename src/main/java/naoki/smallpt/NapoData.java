package naoki.smallpt;

public class NapoData {
    //頂点のデータ
    static double[] cod = {
       28,-20,  5,   30,-15,  7,   30,-10,  5,   45, -2,  7,//0-15
       45,  8,  7,   25, 15,  5,   15,-20,  5,   20,-15,  8,
       20,-10,  7,   10, -2, 15,   10, 10, 15,   10, 15, 10,
       27, -8,  7,   27, -8, 12,    0, -5, 20,    0, 10, 20,

        0,-22,  0,    0,-22,  5,   -7,-15,  8,  -10,-10, 12,//16-31
      -10,  0, 12,  -10, 10, 12,  -10, 15,  8,  -10, 15,  0,
      -25,-10,  0,  -25,-10,  5,  -25, -5,  5,  -25,  0,  5,
      -25,  7,  5,  -25,  7,  0,  -35,-10,  0,  -35,  7,  0,

       28,-20, -5,   30,-15, -7,   30,-10, -5,   45, -2, -7,//32-47
       45,  8, -7,   25, 15, -5,   15,-20, -5,   20,-15, -8,
       20,-10, -7,   10, -2,-15,   10, 10,-15,   10, 15,-10,
       27, -8, -7,   27, -8,-12,    0, -5,-20,    0, 10,-20,

        0,-22, -0,    0,-22, -5,   -7,-15, -8,  -10,-10,-12,//48-63
      -10,  0,-12,  -10, 10,-12,  -10, 15, -8,  -10, 15, -0,
      -25,-10, -0,  -25,-10, -5,  -25, -5, -5,  -25,  0, -5,
      -25,  7, -5,  -25,  7, -0,  -35,-10, -0,  -35,  7, -0,

      -45,-15,  0,  -45, 10,  0,  -30,-20,  0,  -30,-15,  0,//64-69
      -30, 20,  0,  -20, 20,  0
    };

    //面のデータ
    static int[] jun = {
      30,57,25,25, 8,  57,49,17,25, 7,  49,38, 6,17, 8,  38,32, 0, 6, 7,//上下前後
      32,33, 1, 0, 8,  33,34, 2, 1, 7,  34,35, 3, 2, 8,  35,36, 4, 3, 7,
      36,37, 5, 4, 4,  37,43,11, 5, 2,  43,54,22,11, 2,  54,60,28,22, 1,
      60,31,28,28, 2,  66,16,24,67,10,  29,23,69,68,10,  64,30,31,65,10,
                       16,66,67,24,10,  23,29,68,69,10,  30,64,65,31,10,//ひれ

       6, 0, 7, 7, 6,   0, 1, 7, 7, 6,   7, 1, 8, 8, 6,   1, 2, 8, 8, 6,//右
       8, 2, 9, 9, 7,   2, 3, 9, 9, 7,   3, 4,10, 9, 5,  10, 5,11,11, 5,
       7,17, 6, 6, 6,   8,17, 7, 7, 6,  17, 8,18,18, 7,  18, 8,19,19, 7,
       9,19, 8, 8, 6,  19, 9,20,20, 5,  20, 9,10,21, 5,  21,10,11,11, 4,
      21,11,22,22, 4,  14, 9,10,15,10,  25,17,18,18, 5,  25,18,19,19, 5,
      25,19,21,28, 4,  28,21,22,22, 3,  30,25,28,31, 4,   4, 5,10,10, 4,
       9,14,15,10,10,

      32,38,39,39, 6,  33,32,39,39, 6,  33,39,40,40, 6,  34,33,40,40, 6,//左
      34,40,41,41, 7,  35,34,41,41, 7,  36,35,41,42, 5,  37,42,43,43, 5,
      49,39,38,38, 6,  49,40,39,39, 6,  40,49,50,50, 7,  40,50,51,51, 7,
      51,41,40,40, 6,  41,51,52,52, 5,  41,52,53,42, 5,  42,53,43,43, 4,
      43,53,54,54, 4,  41,46,47,42,10,  49,57,50,50, 5,  50,57,51,51, 5,
      51,57,60,53, 4,  53,60,54,54, 3,  57,62,63,60, 4,  37,36,42,42, 4,
      46,41,42,47,10
    };
}
