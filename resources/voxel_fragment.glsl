
varying vec2 position;

uniform vec2 screenDimensions;
uniform vec3 cameraOrigin;
uniform vec3 cameraTarget;

uniform float time;
uniform int shadingLevel;
uniform int shape;

const int SHADING_SOLID = 0;
const int SHADING_PHONG_LIKE = 1;
const int SHADING_PHONG_LIKE_WITH_SHADOWS = 2;

const int Spheres = 0;
const int Cubes = 1;
const int Torus = 2;
const int Plane = 3;
const int SkewedPlane = 4;
const int Cylinder = 5;

bool cube(ivec3 position, ivec3 cubePos, ivec3 cubeSize) {
	ivec3 cubeMin = cubePos;
	ivec3 cubeMax = cubePos + cubeSize;
	return position.x >= cubeMin.x && position.y >= cubeMin.y && position.z >= cubeMin.z
		&& position.x <= cubeMax.x && position.y <= cubeMax.y && position.z <= cubeMax.z;
}

/** Return signed distance from voxel. negative means voxel found, positive means still far. */
bool voxelAt(ivec3 p) {
	// Cylinder and torus equations derived from https://www.shadertoy.com/view/Xds3zN under MIT License
	if (shape == Spheres) {
		return length(vec3(p)) < 12.0 || length(vec3(p+ivec3(20, 0, 0))) < 6.0;
	} else if (shape == Cubes) {
		return cube(p, ivec3(-3, 1, -1), ivec3(3)) || cube(p, ivec3(2, 0, 0), ivec3(8));
	} else if (shape == Torus) {
		float radius = 12.0 + sin(time * 0.1) * 7.0;
		float internalRadius = radius / 3.0;
		vec3 pf = vec3(p);
		return length(vec2(length(pf.xz)-radius, pf.y)) < internalRadius;
	} else if (shape == Plane) {
		return p.y == 0;
	} else if (shape == SkewedPlane) {
     	return p.x + p.y + p.z == 0;
    } else if (shape == Cylinder) {
        vec3 pf = vec3(p);
        vec2 h = vec2(12.0 + sin(time * 0.1) * 10.0, 12.0 + cos(time * 0.3) * 10.0);
        vec2 d = abs(vec2(length(pf.xz),pf.y)) - h;
        return min(max(d.x,d.y),0.0) + length(max(d,0.0)) < 0.0;
	}
	return false;
}

float d2c(float currentPos, float ray) {
	if (ray >= 0.0) {
        // Ray is positive
        return floor(currentPos + 1.0) - currentPos;
    } else {
        // Ray is negative
        return currentPos - ceil(currentPos - 1.0);
    }
}

vec3 deltaToCross(vec3 pos, vec3 ray) {
	return vec3(
		d2c(pos.x, ray.x),
		d2c(pos.y, ray.y),
		d2c(pos.z, ray.z)
	);
}

int toPixelPos(float p, float ray) {
	float whole = floor(p);
	if (abs(whole - p) < 1E-7 && ray < 0.0) {
		return int(whole) - 1;
	} else {
		return int(whole);
	}
}

struct TraceResult {
	ivec3 voxel;
	vec3 uvw;
	vec3 normal;
	bool hit;
};

TraceResult trace(vec3 origin, vec3 ray, float tLimit) {
	ivec3 direction = ivec3(sign(ray));
	vec3 absRay = abs(ray);
	vec3 invAbsRay = 1.0 / absRay;

	float totalT = 0.0;

	vec3 remainingToCross = deltaToCross(origin, ray);
	ivec3 voxel = ivec3(toPixelPos(origin.x, ray.x),
						toPixelPos(origin.y, ray.y),
						toPixelPos(origin.z, ray.z));

	while(totalT < tLimit) {
		vec3 ts = remainingToCross * invAbsRay;
		ivec3 crossed = ivec3(0);

		if (ts.x < ts.y && ts.x < ts.z) {
			// Crossing X first
			crossed.x = -1;
			totalT += ts.x;

			voxel.x += direction.x;
			remainingToCross.x = 1.0;
			remainingToCross.y -= ts.x * absRay.y;
			remainingToCross.z -= ts.x * absRay.z;
		} else if (ts.y < ts.z) {
            // Crossing Y first
			totalT += ts.y;
			crossed.y = -1;

            voxel.y += direction.y;
            remainingToCross.x -= ts.y * absRay.x;
            remainingToCross.y = 1.0;
            remainingToCross.z -= ts.y * absRay.z;
        } else {
            // Crossing Z first
			totalT += ts.z;
			crossed.z = -1;

            voxel.z += direction.z;
            remainingToCross.x -= ts.z * absRay.x;
            remainingToCross.y -= ts.z * absRay.y;
            remainingToCross.z = 1.0;
        }

		if (voxelAt(voxel)) {
			TraceResult result;
			result.voxel = voxel;
			result.uvw = vec3(ray.x > 0.0 ? (1.0 - remainingToCross.x) : remainingToCross.x,
						 	 ray.y > 0.0 ? (1.0 - remainingToCross.y) : remainingToCross.y,
							 ray.z > 0.0 ? (1.0 - remainingToCross.z) : remainingToCross.z);
			result.normal = vec3(direction * crossed);
			result.hit = true;

			return result;
		}
	}

	TraceResult nonHitResult;
	nonHitResult.hit = false;
	return nonHitResult;
}

// Camera to world tranformation, calcNormal, based on: https://www.shadertoy.com/view/Xds3zN
mat3 setCamera (in vec3 origin, in vec3 target, float roll) {
	vec3 cw = normalize(target-origin);
	vec3 cp = vec3(sin(roll), cos(roll),0.0);
	vec3 cu = normalize( cross(cw,cp) );
	vec3 cv = normalize( cross(cu,cw) );
    return mat3( cu, cv, cw );
}

void main() {
	// Create ray
    mat3 camera = setCamera(cameraOrigin, cameraTarget, 0.0);
	vec2 pos = (position * screenDimensions) / screenDimensions.y;
    vec3 ray = camera * normalize( vec3(pos.xy,2.0) );

	vec3 lightPos = vec3(sin(time*0.5) * 100.0, 100.0, cos(time*0.5) * 100.0);
	vec3 lightNormal = normalize(lightPos);

	vec3 color;

    // render
    TraceResult result = trace(cameraOrigin, ray, 100.0);
    if (result.hit) {
        if (shadingLevel <= SHADING_SOLID) {
            color = result.uvw * 0.5;
        } else {
            vec3 reflected = reflect(ray, result.normal);
            float diffuse = max(dot(result.normal, lightNormal), 0.0) * 0.4;
            float specular = pow(max(dot(reflected, lightNormal), 0.0), 20.0) * 0.9;

            if (shadingLevel >= SHADING_PHONG_LIKE_WITH_SHADOWS) {
                vec3 pixelPos = vec3(result.voxel) + result.uvw;
                vec3 directionToLight = normalize(lightPos - pixelPos);
                TraceResult shadowResult = trace(pixelPos + directionToLight * 60.0, -directionToLight, 65.0);
                if (!(shadowResult.hit && shadowResult.voxel == result.voxel)) {
                    diffuse *= 0.2;
                    specular = 0.0;
                }
            }

            color = result.uvw * 0.3 + vec3(diffuse) + vec3(specular);
        }
    } else {
        vec3 groundNear = vec3(0.14, 0.41, 0.16);
        vec3 groundFar = vec3(0.3, 0.53, 0.35);
        vec3 skyLow = vec3(0.5, 0.9, 0.9);
        vec3 skyHigh = vec3(0.12, 0.24, 0.92);
        if (ray.y < 0.0) {
            color = mix(groundFar, groundNear, -ray.y);
        } else {
            color = mix(skyLow, skyHigh, ray.y);
        }

        float specular = pow(max(dot(ray, lightNormal), 0.0), 120.0);
        color += vec3(specular);
    }

    gl_FragColor = vec4(color, 1.0);
}