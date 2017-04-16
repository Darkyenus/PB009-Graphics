attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_projViewTrans;
uniform mat3 u_normalMatrix;
uniform mat4 u_worldTrans;

varying vec3 v_normal;

void main() {
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	gl_Position = u_projViewTrans * pos;
	v_normal = normal;
}
