#pragma version(1)
#pragma rs java_package_name(com.example.background)

rs_allocation in;
rs_allocation out;
rs_script script;
uint32_t width;
uint32_t height;
const static int window_size = 15;

void root(const uchar4* v_in, uchar4* v_out, const void* usrData, uint32_t x, uint32_t y) {
  // indexed by intensity
  int count[256] = {0};
  int totalr = 0;
  int totalg = 0;
  int totalb = 0;
  int max_intensity = -1;
  int max_intensity_count = 0;
  for(int xoffset = (-1 * window_size) / 2; xoffset <= (window_size / 2); xoffset++) {
    for(int yoffset = (-1 * window_size) / 2; yoffset <= (window_size / 2); yoffset++) {
      int calcX = x + xoffset;
      int calcY = y + yoffset;
      if ((calcX >= 0 && calcX < width) && (calcY >=0 && calcY < height)) {
        // get pixel
        uchar4* pixel = (uchar4*) rsGetElementAt(in, calcX, calcY);
        int intensity = (pixel->r + pixel->g + pixel->b) / 3;
        // increment intensity count
        int value = count[intensity];
        value++;
        count[intensity] = value;
        if (max_intensity_count < value) {
          max_intensity_count = value;
          max_intensity = intensity;
        }
      }
    }
  }
  for(int xoffset = (-1 * window_size) / 2; xoffset <= (window_size / 2); xoffset++) {
    for(int yoffset = (-1 * window_size) / 2; yoffset <= (window_size / 2); yoffset++) {
      int calcX = x + xoffset;
      int calcY = y + yoffset;
      if ((calcX >= 0 && calcX < width) && (calcY >=0 && calcY < height)) {
        // get pixel
        uchar4* pixel = (uchar4*) rsGetElementAt(in, calcX, calcY);
        int intensity = (pixel->r + pixel->g + pixel->b) / 3;
        if (intensity == max_intensity) {
          totalr += pixel->r;
          totalg += pixel->g;
          totalb += pixel->b;
        }
      }
    }
  }
  v_out->r = totalr / max_intensity_count;
  v_out->g = totalg / max_intensity_count;
  v_out->b = totalb / max_intensity_count;
  v_out->a = v_in->a;
}

void filter() {
  rsDebug("Processing image with dimensions for water color effect", width, height);
  rsForEach(script, in, out);
}
