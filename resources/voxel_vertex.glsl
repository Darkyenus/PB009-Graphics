attribute vec2 in_position;
varying vec2 position;

void main() {
	position = in_position.xy;
	gl_Position = vec4(in_position, 0.0, 1.0);
}