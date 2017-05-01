//-----------------------------------------------------------------------------------------------
//                  Noise functions
//-----------------------------------------------------------------------------------------------

//
// Description : Array and textureless GLSL 2D simplex noise function.
//      Author : Ian McEwan, Ashima Arts.
//  Maintainer : stegu
//     Lastmod : 20110822 (ijm)
//     License : Copyright (C) 2011 Ashima Arts. All rights reserved.
//               Distributed under the MIT License. See LICENSE file.
//               https://github.com/ashima/webgl-noise
//               https://github.com/stegu/webgl-noise
//

vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
  return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v)
  {
  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626,  // -1.0 + 2.0 * C.x
                      0.024390243902439); // 1.0 / 41.0
// First corner
  vec2 i  = floor(v + dot(v, C.yy) );
  vec2 x0 = v -   i + dot(i, C.xx);

// Other corners
  vec2 i1;
  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
  //i1.y = 1.0 - i1.x;
  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  // x0 = x0 - 0.0 + 0.0 * C.xx ;
  // x1 = x0 - i1 + 1.0 * C.xx ;
  // x2 = x0 - 1.0 + 2.0 * C.xx ;
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

// Permutations
  i = mod289(i); // Avoid truncation effects in permutation
  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
		+ i.x + vec3(0.0, i1.x, 1.0 ));

  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
  m = m*m ;
  m = m*m ;

// Gradients: 41 points uniformly over a line, mapped onto a diamond.
// The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;

// Normalise gradients implicitly by scaling m
// Approximation of: m *= inversesqrt( a0*a0 + h*h );
  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

// Compute final noise value at P
  vec3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

//-----------------------------------------------------------------------------------------------


varying vec2 position;

uniform vec2 screenDimensions;
uniform vec3 cameraOrigin;
uniform vec3 cameraTarget;

uniform float time;
uniform int shadingLevel;

const int SHADING_A = 0;
const int SHADING_AD = 1;
const int SHADING_ADS = 2;

vec3 lightPos;
vec3 lightNormal;
const float ambientContribution = 0.2;

const vec3 TEXTURE = vec3(-1.0, 0.0, 0.0);
const vec3 NORMAL = vec3(0.0, -1.0, 0.0);

struct Material {
	vec3 diffuseColor;
	float reflectivity;
	float specularity;
};

const Material MAT_SOLID_RED = Material(vec3(1.0,0.0,0.0), 0.0, 20.0);
const Material MAT_MIRROR = Material(vec3(0.0,0.0,0.0), 1.0, 0.0);
const Material MAT_MARBLE = Material(TEXTURE, 0.05, 0.4);
const Material MAT_SOLID_NORMAL = Material(NORMAL, 0.0, 0.0);

struct MapResult {
	float distance;
	Material nearestObjectMaterial;
};

// Shape equations derived from https://www.shadertoy.com/view/Xds3zN under MIT License

MapResult box(vec3 p, vec3 boxSize, Material boxMaterial) {
	MapResult result;
	vec3 d = abs(p) - boxSize;
    result.distance = min(max(d.x,max(d.y,d.z)),0.0) + length(max(d,0.0));
    result.nearestObjectMaterial = boxMaterial;
	return result;
}

MapResult sphere(vec3 p, float sphereRadius, Material sphereMaterial) {
	MapResult result;
	result.distance = length(p) - sphereRadius;
	result.nearestObjectMaterial = sphereMaterial;
	return result;
}

MapResult and(MapResult result1, MapResult result2) {
	if (result1.distance < result2.distance) {
		return result1;
	} else {
		return result2;
	}
}

MapResult andNot(MapResult with, MapResult without) {
	if (with.distance > -without.distance) {
		return with;
	} else {
		without.distance = -without.distance;
		return without;
	}
}

MapResult map(vec3 p) {
	MapResult r = box(p, vec3(59.0), MAT_SOLID_RED);
	r = andNot(r, box(p - vec3(22.0, 0.0, 0.0), vec3(56.0), MAT_SOLID_NORMAL));
	r = and(r, box(p - vec3(-36.0, 0.0, 0.0), vec3(2.0, 58.0, 58.0), MAT_MARBLE));
	r = and(r, sphere(p - vec3(15.0, -29.0, -30.0), 25.0, MAT_MIRROR));
	r = and(r, sphere(p - vec3(-15.0, 15.0, 25.0), 20.0, MAT_MIRROR));
	return r;

	/*MapResult r = sphere(p - vec3(0.0), 10.0, MAT_SOLID_RED);
	r = and(r, sphere(p - vec3(8.0, 0.0, 0.0), 5.0, MAT_MIRROR));
	r = and(r, sphere(p - vec3(20.0, 0.0, 0.0), 2.0, MAT_SOLID_RED));
	r = andNot(r, box(p - vec3(0.0, 0.0, 10.0), vec3(6.0), MAT_MIRROR));
	r = and(r, sphere(p - vec3(0.0, 0.0, 10.0), 2.0, MAT_SOLID_RED));
	return r;*/
}

struct TraceResult {
	vec3 ray;
	vec3 position;
	float t;
	Material material;
	bool hit;
};

TraceResult trace(vec3 origin, vec3 ray, int iterationLimit) {
	TraceResult traceResult;
	traceResult.position = origin;
	traceResult.ray = ray;
	traceResult.t = 0.0;

	int i = 0;
	for (;;) {
		MapResult mapResult = map(traceResult.position);

		if (mapResult.distance <= 1E-5) {
			//HIT
			traceResult.material = mapResult.nearestObjectMaterial;
			traceResult.hit = true;
			return traceResult;
		}

		traceResult.position += mapResult.distance * ray;

		i++;
		if (i >= iterationLimit) {
			if (mapResult.distance < 1.5) {
				// Maybe would be hit
				traceResult.material = mapResult.nearestObjectMaterial;
	            traceResult.hit = true;
	            return traceResult;
			}
			break;
		}
	}

	traceResult.hit = false;
	return traceResult;
}

// Camera to world tranformation and calcNormal, based on: https://www.shadertoy.com/view/Xds3zN
mat3 setCamera (in vec3 origin, in vec3 target, float roll) {
	vec3 cw = normalize(target-origin);
	vec3 cp = vec3(sin(roll), cos(roll),0.0);
	vec3 cu = normalize( cross(cw,cp) );
	vec3 cv = normalize( cross(cu,cw) );
    return mat3( cu, cv, cw );
}

vec3 calcNormal( in vec3 pos )
{
    vec2 e = vec2(1.0,-1.0)*0.5773*0.0005;
    return normalize( e.xyy*map( pos + e.xyy ).distance +
					  e.yyx*map( pos + e.yyx ).distance +
					  e.yxy*map( pos + e.yxy ).distance +
					  e.xxx*map( pos + e.xxx ).distance );
}
//

vec3 groundColor(vec3 ray) {
	vec3 groundNear = vec3(0.14, 0.41, 0.16);
    vec3 groundFar = vec3(0.3, 0.53, 0.35);
    vec3 skyLow = vec3(0.5, 0.9, 0.9);
    vec3 skyHigh = vec3(0.12, 0.24, 0.92);
    vec3 color;
    if (ray.y < 0.0) {
        color = mix(groundFar, groundNear, -ray.y);
    } else {
        color = mix(skyLow, skyHigh, ray.y);
    }

    float specular = pow(max(dot(ray, lightNormal), 0.0), 120.0);
    color += vec3(specular);
    return color;
}

vec3 traceColor(vec3 origin, vec3 ray, int iterationLimit) {
	const int reflectionLimit = 8;
	TraceResult traces[reflectionLimit];
	vec3 hitNormals[reflectionLimit];

	vec3 traceOrigin = origin;
	vec3 traceRay = ray;

	// Create trace stack
	int reflectionDepth = 0;
	while (reflectionDepth < reflectionLimit) {
		TraceResult tResult = trace(traceOrigin, traceRay, iterationLimit);
		if (tResult.hit) {
			vec3 normal = calcNormal(tResult.position);

			traceRay = reflect(traceRay, normal);
			traceOrigin = tResult.position + traceRay * 1E-1;

			traces[reflectionDepth] = tResult;
			hitNormals[reflectionDepth] = normal;

			reflectionDepth++;

			if (tResult.material.reflectivity > 0.0) {
				continue;
			}
		}
		break;
	}

	// Pop the trace stack
	vec3 color = groundColor(traceRay);

	for (--reflectionDepth; reflectionDepth >= 0; --reflectionDepth) {
		TraceResult tResult = traces[reflectionDepth];
		vec3 normal = hitNormals[reflectionDepth];

		Material material = tResult.material;
		float reflectivity = material.reflectivity;

		float diffuseContribution;
		if (shadingLevel <= SHADING_A) {
			diffuseContribution = 0.8;
		} else {
			diffuseContribution = max((dot(normal, lightNormal) + 0.9) / 1.9, ambientContribution);
		}
		vec3 specularTestRay = reflect(traceRay, normal);
		float specularContribution;
		if (shadingLevel <= SHADING_AD) {
			specularContribution = 0.0;
		} else {
			specularContribution= material.specularity <= 0.0 ? 0.0 : pow(max(dot(specularTestRay, lightNormal), 0.0), material.specularity);
		}

		//Shadow
		/*{
			vec3 toLight = normalize(lightPos - tResult.position);
			TraceResult shadowTrace = trace(tResult.position + toLight * 0.0001, toLight, 40);
			if (shadowTrace.hit) {
				//In shade
				diffuseContribution /= 2.0;
				specularContribution = 0.0;
			}
		}*/

		vec3 diffuseColorBase = material.diffuseColor;
		if (diffuseColorBase == TEXTURE) {
			float baseNoise = snoise(tResult.position.zy * vec2(1.0/(14.0 + sin(time * 0.1) * 2.0), 1.0/14.0) - tResult.position.xz * vec2(0.1, -0.1) + vec2(sin(time * 0.01) * 10.0, sin(time * 0.01 + 15.0) * 10.0));
			float baseNoise2 = snoise(tResult.position.zy * vec2(1.0/(12.0 - sin(time * 0.1) * 2.0), 1.0/5.0) - tResult.position.xz * vec2(-0.6, 0.01) + vec2(sin(time * 0.04) * 5.7, sin(time * 0.1 - 100.0) * 4.0));
			baseNoise += sin(time * 0.5) * 0.2;

			baseNoise = baseNoise + baseNoise2;

			if (baseNoise < 0.0) {
				diffuseColorBase = mix(vec3(0.7, 0.67, 0.62), vec3(0.92, 0.98, 0.77), -baseNoise);
			} else {
				diffuseColorBase = mix(vec3(0.3, 0.22, 0.3), vec3(0.2, 0.08, 0.23), baseNoise);
			}
		} else if (diffuseColorBase == NORMAL) {
			if (normal.x > 0.5) {
				diffuseColorBase = vec3(1.0, 0.0, 0.0);
			} else if (normal.y > 0.5) {
                diffuseColorBase = vec3(0.0, 1.0, 0.0);
            } else if (normal.z > 0.5) {
	            diffuseColorBase = vec3(0.0, 0.0, 1.0);
	        } else if (normal.x < -0.5) {
				diffuseColorBase = vec3(0.0, 1.0, 1.0);
			} else if (normal.y < -0.5) {
				diffuseColorBase = vec3(1.0, 0.0, 1.0);
			} else {
				diffuseColorBase = vec3(1.0, 1.0, 0.0);
			}
		}

		vec3 diffuseColor = diffuseContribution * diffuseColorBase;
		color = mix(diffuseColor, color, reflectivity);
		color += specularContribution * vec3(0.5);

		traceRay = tResult.ray;
	}

    return color;
}

void main() {
	// Create ray
    mat3 camera = setCamera(cameraOrigin, cameraTarget, 0.0);
	vec2 pos = (position * screenDimensions) / screenDimensions.y;
    vec3 ray = camera * normalize( vec3(pos.xy,2.0) );

	lightPos = vec3(50.0, 0.0, 0.0) + vec3(0.0, cos(time*0.4) * 20.0, sin(time*0.5) * 30.0);
	lightNormal = normalize(lightPos);

	vec3 color = traceColor(cameraOrigin, ray, 75);

    gl_FragColor = vec4(color, 1.0);
}