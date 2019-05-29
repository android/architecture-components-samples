#pragma version(1)
#pragma rs java_package_name(com.example.background)

rs_allocation in;
rs_allocation out;
rs_script script;

uint32_t width;
uint32_t height;

void root(const uchar4* v_in, uchar4* v_out, const void* usrData, uint32_t x, uint32_t y) {
  int r = v_in->r;
  int g = v_in->g;
  int b = v_in->b;
  int gray = (r+g+b)/3;
  v_out->r = gray;
  v_out->g = gray;
  v_out->b = gray;
  v_out->a = v_in->a;
}

void filter() {
  rsDebug("Processing image with dimensions for grayscale", width, height);
  rsForEach(script, in, out);
}
